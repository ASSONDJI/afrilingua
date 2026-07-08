"""
SM-2 spaced repetition algorithm (the algorithm behind Anki), used to
schedule the next review date for a word a learner has already seen.

Unlike clustering/PCA/difficulty extraction, this module requires no
training data at all — it is a deterministic scheduling function driven
purely by the learner's own answer quality on each review.
"""
from dataclasses import dataclass
from datetime import datetime, timedelta


@dataclass
class ReviewState:
    repetitions: int
    ease_factor: float
    interval_days: int


@dataclass
class ReviewResult:
    next_review_at: datetime
    new_state: ReviewState


class SrsService:
    """
    quality: 0-5 self-assessed recall quality (SM-2 convention).
      < 3 means the learner failed to recall the word — restart the interval.
      >= 3 means a successful recall of increasing difficulty.
    """

    MIN_EASE_FACTOR = 1.3

    def review(self, state: ReviewState, quality: int, now: datetime | None = None) -> ReviewResult:
        if not 0 <= quality <= 5:
            raise ValueError("quality must be between 0 and 5")

        now = now or datetime.utcnow()

        if quality < 3:
            new_state = ReviewState(repetitions=0, ease_factor=state.ease_factor, interval_days=1)
        else:
            if state.repetitions == 0:
                interval = 1
            elif state.repetitions == 1:
                interval = 6
            else:
                interval = round(state.interval_days * state.ease_factor)

            new_ease = state.ease_factor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
            new_ease = max(new_ease, self.MIN_EASE_FACTOR)

            new_state = ReviewState(
                repetitions=state.repetitions + 1,
                ease_factor=new_ease,
                interval_days=interval,
            )

        return ReviewResult(
            next_review_at=now + timedelta(days=new_state.interval_days),
            new_state=new_state,
        )
