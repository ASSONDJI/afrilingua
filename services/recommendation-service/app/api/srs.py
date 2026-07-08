from fastapi import APIRouter

from app.models.schemas import ReviewStateModel, SrsReviewRequest, SrsReviewResponse
from app.services.srs_service import ReviewState, SrsService

router = APIRouter()
_service = SrsService()


@router.post("/review", response_model=SrsReviewResponse)
def review_word(request: SrsReviewRequest) -> SrsReviewResponse:
    state = ReviewState(
        repetitions=request.state.repetitions,
        ease_factor=request.state.ease_factor,
        interval_days=request.state.interval_days,
    )
    result = _service.review(state, request.quality)

    return SrsReviewResponse(
        next_review_at=result.next_review_at,
        new_state=ReviewStateModel(
            repetitions=result.new_state.repetitions,
            ease_factor=result.new_state.ease_factor,
            interval_days=result.new_state.interval_days,
        ),
    )
