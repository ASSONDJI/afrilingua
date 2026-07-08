from datetime import datetime, timezone

import pytest

from app.services.srs_service import ReviewState, SrsService


def test_failed_recall_resets_repetitions_and_interval():
    service = SrsService()
    state = ReviewState(repetitions=3, ease_factor=2.5, interval_days=15)

    result = service.review(state, quality=1)

    assert result.new_state.repetitions == 0
    assert result.new_state.interval_days == 1


def test_first_successful_review_sets_interval_to_one_day():
    service = SrsService()
    state = ReviewState(repetitions=0, ease_factor=2.5, interval_days=0)

    result = service.review(state, quality=4)

    assert result.new_state.repetitions == 1
    assert result.new_state.interval_days == 1


def test_second_successful_review_sets_interval_to_six_days():
    service = SrsService()
    state = ReviewState(repetitions=1, ease_factor=2.5, interval_days=1)

    result = service.review(state, quality=4)

    assert result.new_state.repetitions == 2
    assert result.new_state.interval_days == 6


def test_third_successful_review_multiplies_interval_by_ease_factor():
    service = SrsService()
    state = ReviewState(repetitions=2, ease_factor=2.5, interval_days=6)

    result = service.review(state, quality=4)

    assert result.new_state.repetitions == 3
    assert result.new_state.interval_days == 15  # 6 * 2.5


def test_ease_factor_never_drops_below_minimum():
    service = SrsService()
    state = ReviewState(repetitions=2, ease_factor=1.3, interval_days=6)

    result = service.review(state, quality=0)

    assert result.new_state.ease_factor >= SrsService.MIN_EASE_FACTOR


def test_next_review_at_is_computed_from_provided_now():
    service = SrsService()
    state = ReviewState(repetitions=0, ease_factor=2.5, interval_days=0)
    fixed_now = datetime(2026, 1, 1, tzinfo=timezone.utc)  # ← Correction : bien indenté

    result = service.review(state, quality=4, now=fixed_now)

    assert result.next_review_at == datetime(2026, 1, 2, tzinfo=timezone.utc)


def test_invalid_quality_raises():
    service = SrsService()
    state = ReviewState(repetitions=0, ease_factor=2.5, interval_days=0)

    with pytest.raises(ValueError, match="quality must be between 0 and 5"):
        service.review(state, quality=6)