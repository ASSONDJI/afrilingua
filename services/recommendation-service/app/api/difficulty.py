import pandas as pd
from fastapi import APIRouter

from app.models.schemas import (
    DifficultyClassifyRequest,
    DifficultyClassifyResponse,
    DifficultyTrainRequest,
    DifficultyTrainResponse,
)
from app.services.difficulty_service import DifficultyService

router = APIRouter()

# Single shared instance: the rule is cheap to retrain and stateless enough
# that per-request retraining is unnecessary complexity for now.
_service = DifficultyService()


@router.post("/train", response_model=DifficultyTrainResponse)
def train_difficulty_rule(request: DifficultyTrainRequest) -> DifficultyTrainResponse:
    df = pd.DataFrame([w.model_dump() for w in request.words])
    result = _service.fit(df)
    return DifficultyTrainResponse(
        accuracy_on_training_rule=result.accuracy_on_training_rule,
        human_readable_rules=result.human_readable_rules,
        depth=result.depth,
        n_leaves=result.n_leaves,
    )


@router.post("/classify", response_model=DifficultyClassifyResponse)
def classify_word(request: DifficultyClassifyRequest) -> DifficultyClassifyResponse:
    niveau = _service.classify(
        nb_syllabes=request.nb_syllabes,
        longueur_mot=request.longueur_mot,
        ton1=request.ton1,
        ton2=request.ton2,
    )
    return DifficultyClassifyResponse(niveau=niveau)
