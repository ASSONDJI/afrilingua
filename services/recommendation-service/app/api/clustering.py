import pandas as pd
from fastapi import APIRouter

from app.models.schemas import ClusterRequest, ClusterResponse
from app.services.clustering_service import ClusteringService

router = APIRouter()

TONE_ORDER = {"bas": 0, "moyen": 1, "haut": 2}


@router.post("", response_model=list[ClusterResponse])
def cluster_words(request: ClusterRequest) -> list[ClusterResponse]:
    df = pd.DataFrame([w.model_dump() for w in request.words])
    df["ton1_num"] = df["ton1"].map(TONE_ORDER).fillna(0)
    df["ton2_num"] = df["ton2"].map(TONE_ORDER).fillna(0)

    service = ClusteringService(n_clusters=request.n_clusters)
    result = service.fit_predict(df)

    return [
        ClusterResponse(word_id=word_id, cluster=cluster)
        for word_id, cluster in zip(df["word_id"], result.cluster_labels)
    ]
