import pandas as pd
from fastapi import APIRouter

from app.models.schemas import PcaPoint, PcaRequest, PcaResponse
from app.services.pca_service import PcaService

router = APIRouter()

TONE_ORDER = {"bas": 0, "moyen": 1, "haut": 2}


@router.post("", response_model=PcaResponse)
def project_words(request: PcaRequest) -> PcaResponse:
    df = pd.DataFrame([w.model_dump() for w in request.words])
    df["ton1_num"] = df["ton1"].map(TONE_ORDER).fillna(0)
    df["ton2_num"] = df["ton2"].map(TONE_ORDER).fillna(0)

    service = PcaService()
    result = service.fit_transform(df)

    points = [
        PcaPoint(word_id=word_id, x=coord[0], y=coord[1])
        for word_id, coord in zip(df["word_id"], result.coordinates)
    ]
    return PcaResponse(points=points, explained_variance_ratio=result.explained_variance_ratio)

