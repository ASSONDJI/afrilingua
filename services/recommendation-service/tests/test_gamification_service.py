from datetime import date, timedelta

import pytest

from app.db.entities import Progression, UserBadge, LearnedWord
from app.services.gamification_service import (
    compute_level,
    handle_quiz_completed,
    handle_lesson_completed,
    _update_streak,
)


def _get_progression(db_session, user_id):
    return db_session.query(Progression).filter_by(user_id=user_id).one()


def _badge_codes(db_session, progression_id):
    rows = (
        db_session.query(UserBadge)
        .filter_by(progression_id=progression_id)
        .all()
    )
    return {row.badge.code for row in rows}


class TestComputeLevel:
    def test_zero_xp_is_level_one(self):
        assert compute_level(0) == 1

    def test_ninety_nine_xp_is_still_level_one(self):
        assert compute_level(99) == 1

    def test_one_hundred_xp_is_level_two(self):
        assert compute_level(100) == 2


class TestUpdateStreak:
    def test_first_ever_activity_sets_streak_to_one(self):
        progression = Progression(current_streak=0, longest_streak=0, last_activity_date=None)

        _update_streak(progression, date(2026, 1, 1))

        assert progression.current_streak == 1
        assert progression.longest_streak == 1

    def test_consecutive_day_increments_streak(self):
        progression = Progression(current_streak=3, longest_streak=3, last_activity_date=date(2026, 1, 1))

        _update_streak(progression, date(2026, 1, 2))

        assert progression.current_streak == 4
        assert progression.longest_streak == 4

    def test_same_day_does_not_double_increment(self):
        progression = Progression(current_streak=3, longest_streak=5, last_activity_date=date(2026, 1, 1))

        _update_streak(progression, date(2026, 1, 1))

        assert progression.current_streak == 3
        assert progression.longest_streak == 5

    def test_gap_of_more_than_one_day_resets_streak(self):
        progression = Progression(current_streak=10, longest_streak=10, last_activity_date=date(2026, 1, 1))

        _update_streak(progression, date(2026, 1, 5))

        assert progression.current_streak == 1
        assert progression.longest_streak == 10  # le record n'est pas efface

    def test_longest_streak_updates_when_current_exceeds_it(self):
        progression = Progression(current_streak=2, longest_streak=2, last_activity_date=date(2026, 1, 1))

        _update_streak(progression, date(2026, 1, 2))

        assert progression.current_streak == 3
        assert progression.longest_streak == 3


class TestHandleLessonCompleted:
    def test_first_lesson_grants_fifty_xp_and_first_lesson_badge(self, db_session, make_user_id):
        user_id = make_user_id()

        result = handle_lesson_completed(db_session, {"user_id": str(user_id)})

        assert result["xp_gained"] == 50
        assert result["total_xp"] == 50
        assert result["level"] == 1
        assert result["current_streak"] == 1
        assert "first_lesson" in result["new_badges"]

        progression = _get_progression(db_session, user_id)
        assert progression.lessons_completed == 1

    def test_second_lesson_does_not_regrant_first_lesson_badge(self, db_session, make_user_id):
        user_id = make_user_id()
        handle_lesson_completed(db_session, {"user_id": str(user_id)})

        result = handle_lesson_completed(db_session, {"user_id": str(user_id)})

        assert "first_lesson" not in result["new_badges"]
        assert result["total_xp"] == 100

    def test_seven_consecutive_days_grants_streak_seven_badge(self, db_session, make_user_id):
        user_id = make_user_id()
        start = date(2026, 1, 1)

        progression = None
        for offset in range(7):
            handle_lesson_completed(
                db_session,
                {"user_id": str(user_id)},
                event_date=start + timedelta(days=offset),
            )
            progression = _get_progression(db_session, user_id)

        codes = _badge_codes(db_session, progression.id)
        assert "streak_7" in codes
        assert progression.current_streak == 7


class TestHandleQuizCompleted:
    def test_correct_answers_grant_ten_xp_each(self, db_session, make_user_id):
        user_id = make_user_id()

        result = handle_quiz_completed(
            db_session,
            {
                "user_id": str(user_id),
                "correct_word_ids": [str(make_user_id()), str(make_user_id())],
                "is_perfect": False,
            },
        )

        assert result["xp_gained"] == 20
        assert result["total_xp"] == 20

    def test_perfect_quiz_adds_bonus_and_grants_badge(self, db_session, make_user_id):
        user_id = make_user_id()

        result = handle_quiz_completed(
            db_session,
            {
                "user_id": str(user_id),
                "correct_word_ids": [str(make_user_id())],
                "is_perfect": True,
            },
        )

        # 1 mot correct (10) + bonus quiz parfait (20) = 30
        assert result["xp_gained"] == 30
        assert "quiz_perfect" in result["new_badges"]

        progression = _get_progression(db_session, user_id)
        assert progression.has_perfect_quiz is True

    def test_same_word_learned_twice_is_not_double_counted(self, db_session, make_user_id):
        """
        Regression test pour le point de conception explicitement discute :
        le badge words_100 doit compter des mots UNIQUES, pas des bonnes
        reponses cumulees -- sinon reviser 100 fois le meme mot debloquerait
        le badge a tort.
        """
        user_id = make_user_id()
        word_id = str(make_user_id())

        handle_quiz_completed(
            db_session,
            {"user_id": str(user_id), "correct_word_ids": [word_id], "is_perfect": False},
        )
        handle_quiz_completed(
            db_session,
            {"user_id": str(user_id), "correct_word_ids": [word_id], "is_perfect": False},
        )

        progression = _get_progression(db_session, user_id)
        learned_count = (
            db_session.query(LearnedWord)
            .filter_by(progression_id=progression.id)
            .count()
        )
        assert learned_count == 1

    def test_words_100_badge_requires_one_hundred_unique_words(self, db_session, make_user_id):
        user_id = make_user_id()
        word_ids = [str(make_user_id()) for _ in range(100)]

        result = handle_quiz_completed(
            db_session,
            {"user_id": str(user_id), "correct_word_ids": word_ids, "is_perfect": False},
        )

        assert "words_100" in result["new_badges"]

    def test_ninety_nine_unique_words_does_not_grant_words_100_badge(self, db_session, make_user_id):
        user_id = make_user_id()
        word_ids = [str(make_user_id()) for _ in range(99)]

        result = handle_quiz_completed(
            db_session,
            {"user_id": str(user_id), "correct_word_ids": word_ids, "is_perfect": False},
        )

        assert "words_100" not in result["new_badges"]
