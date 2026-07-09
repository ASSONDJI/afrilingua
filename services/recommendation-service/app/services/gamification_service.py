import uuid
from datetime import date, datetime, timedelta
from uuid import UUID

from sqlalchemy import select
from sqlalchemy.dialects.postgresql import insert as pg_insert
from sqlalchemy.orm import Session

from app.db.entities import Progression, Badge, UserBadge, LearnedWord

XP_PER_CORRECT_ANSWER = 10
XP_PER_QUIZ_COMPLETE_BONUS = 20
XP_PER_LESSON_COMPLETE = 50


def compute_level(xp: int) -> int:
    return xp // 100 + 1


def _get_or_create_progression(session: Session, user_id: UUID) -> Progression:
    progression = session.execute(
        select(Progression).where(Progression.user_id == user_id)
    ).scalar_one_or_none()
    if progression is None:
        progression = Progression(user_id=user_id, xp=0, current_streak=0, longest_streak=0, last_activity_date=None)
        session.add(progression)
        session.flush()
    return progression


def _update_streak(progression: Progression, event_date: date) -> None:
    if progression.last_activity_date is None:
        progression.current_streak = 1
    elif progression.last_activity_date == event_date:
        pass  # deja compte aujourd'hui, pas de double increment
    elif progression.last_activity_date == event_date - timedelta(days=1):
        progression.current_streak += 1
    else:
        progression.current_streak = 1  # serie rompue

    if progression.current_streak > progression.longest_streak:
        progression.longest_streak = progression.current_streak

    progression.last_activity_date = event_date


def _record_learned_words(session: Session, progression_id: UUID, word_ids: list[str]) -> None:
    """Insertion idempotente : un mot deja present pour cette progression est ignore
    grace a la contrainte unique (progression_id, word_id), sans lever d'IntegrityError."""
    if not word_ids:
        return

    stmt = pg_insert(LearnedWord).values(
        [
            {
                "id": uuid.uuid4(),
                "progression_id": progression_id,
                "word_id": UUID(word_id),
                "learned_at": datetime.utcnow(),
            }
            for word_id in word_ids
        ]
    )
    stmt = stmt.on_conflict_do_nothing(index_elements=["progression_id", "word_id"])
    session.execute(stmt)


def _count_learned_words(session: Session, progression_id: UUID) -> int:
    result = session.execute(
        select(LearnedWord.id).where(LearnedWord.progression_id == progression_id)
    )
    return len(result.all())


def _check_and_award_badges(session: Session, progression: Progression) -> list[str]:
    """Retourne les codes des badges nouvellement debloques."""
    all_badges = session.execute(select(Badge)).scalars().all()

    already_awarded = {
        row[0] for row in session.execute(
            select(UserBadge.badge_id).where(UserBadge.progression_id == progression.id)
        ).all()
    }

    words_learned_count = _count_learned_words(session, progression.id)

    newly_awarded = []
    for badge in all_badges:
        if badge.id in already_awarded:
            continue

        unlocked = False
        if badge.criteria_type == "streak" and progression.current_streak >= badge.criteria_value:
            unlocked = True
        elif badge.criteria_type == "words_learned" and words_learned_count >= badge.criteria_value:
            unlocked = True
        elif badge.criteria_type == "lessons_completed" and progression.lessons_completed >= badge.criteria_value:
            unlocked = True
        elif badge.criteria_type == "quiz_perfect" and progression.has_perfect_quiz:
            unlocked = True

        if unlocked:
            session.add(UserBadge(progression_id=progression.id, badge_id=badge.id, earned_at=datetime.utcnow()))
            newly_awarded.append(badge.code)

    return newly_awarded


def handle_quiz_completed(session: Session, payload: dict, event_date: date = None) -> dict:
    user_id = UUID(payload["user_id"])
    progression = _get_or_create_progression(session, user_id)

    correct_word_ids = payload.get("correct_word_ids", [])
    xp_gained = len(correct_word_ids) * XP_PER_CORRECT_ANSWER
    if payload.get("is_perfect"):
        xp_gained += XP_PER_QUIZ_COMPLETE_BONUS
        progression.has_perfect_quiz = True

    progression.xp += xp_gained
    progression.level = compute_level(progression.xp)
    _update_streak(progression, event_date or date.today())

    _record_learned_words(session, progression.id, correct_word_ids)

    session.flush()
    newly_awarded = _check_and_award_badges(session, progression)
    session.commit()

    return {
        "xp_gained": xp_gained,
        "total_xp": progression.xp,
        "level": progression.level,
        "current_streak": progression.current_streak,
        "longest_streak": progression.longest_streak,
        "new_badges": newly_awarded,
    }


def handle_lesson_completed(session: Session, payload: dict, event_date: date = None) -> dict:
    user_id = UUID(payload["user_id"])
    progression = _get_or_create_progression(session, user_id)

    progression.xp += XP_PER_LESSON_COMPLETE
    progression.level = compute_level(progression.xp)
    progression.lessons_completed = (progression.lessons_completed or 0) + 1
    _update_streak(progression, event_date or date.today())

    session.flush()
    newly_awarded = _check_and_award_badges(session, progression)
    session.commit()

    return {
        "xp_gained": XP_PER_LESSON_COMPLETE,
        "total_xp": progression.xp,
        "level": progression.level,
        "current_streak": progression.current_streak,
        "longest_streak": progression.longest_streak,
        "new_badges": newly_awarded,
    }
