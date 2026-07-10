from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.entities import Badge
from app.db.session import get_db
from app.models.schemas import BadgeCatalogEntry

router = APIRouter()


@router.get("", response_model=list[BadgeCatalogEntry])
def list_badges(db: Session = Depends(get_db)) -> list[BadgeCatalogEntry]:
    """
    Full badge catalog (fixed, seeded via migration -- see Badge entity
    docstring). Consumed by the mobile app to render locked/unlocked badge
    grids: the app cross-references this against the user's earned badges
    from GET /progressions/{user_id} to know which ones to gray out.
    """
    badges = db.execute(select(Badge).order_by(Badge.criteria_value)).scalars().all()
    return [
        BadgeCatalogEntry(
            code=b.code,
            name=b.name,
            description=b.description,
            criteria_type=b.criteria_type,
            criteria_value=b.criteria_value,
        )
        for b in badges
    ]
