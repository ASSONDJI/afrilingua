from uuid import UUID

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.entities import Progression, UserBadge
from app.db.session import get_db
from app.models.schemas import BadgeInfo, ProgressionResponse

router = APIRouter()


@router.get("/{user_id}", response_model=ProgressionResponse)
def get_progression(user_id: UUID, db: Session = Depends(get_db)) -> ProgressionResponse:
    """
    Read-only view of a user's gamification state. Deliberately returns a
    200 with all-zero defaults rather than a 404 when no progression row
    exists yet -- from the mobile app's point of view, "hasn't done
    anything yet" is the expected initial state for a new user, not an
    error condition to branch on.
    """
    progression = db.execute(
        select(Progression).where(Progression.user_id == user_id)
    ).scalar_one_or_none()

    if progression is None:
        return ProgressionResponse(
            user_id=str(user_id),
            xp=0,
            level=1,
            current_streak=0,
            longest_streak=0,
            lessons_completed=0,
            has_perfect_quiz=False,
            badges=[],
        )

    badge_rows = db.execute(
        select(UserBadge).where(UserBadge.progression_id == progression.id)
    ).scalars().all()

    badges = [
        BadgeInfo(code=row.badge.code, name=row.badge.name, earned_at=row.earned_at)
        for row in badge_rows
    ]

    return ProgressionResponse(
        user_id=str(user_id),
        xp=progression.xp,
        level=progression.level,
        current_streak=progression.current_streak,
        longest_streak=progression.longest_streak,
        lessons_completed=progression.lessons_completed,
        has_perfect_quiz=progression.has_perfect_quiz,
        badges=badges,
    )
