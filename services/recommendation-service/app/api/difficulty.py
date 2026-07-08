import pandas as pd
from fastapi import APIRouter

from app.models.schemas import (
    ApplyRuleRequest,
    ApplyRuleResponse,
    DifficultyTrainRequest,
    DifficultyTrainResponse,
)
from app.services.difficulty_service import DifficultyService, apply_labeling_rule

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


@router.post("/apply-rule", response_model=ApplyRuleResponse)
def apply_rule(request: ApplyRuleRequest) -> ApplyRuleResponse:
    """
    Direct, stateless application of the hand-written tone-based labeling
    rule (see apply_labeling_rule in difficulty_service.py). Unlike
    /classify, this never depends on /train having been called first --
    it IS the ground truth the tree in /train is shown to reproduce.

    This is the endpoint content-service should call when creating a new
    word: it needs an immediate, always-available answer, not a stateful
    model that may or may not have been trained in this process's lifetime.
    """
    niveau = apply_labeling_rule(request.ton1, request.ton2)
    return ApplyRuleResponse(niveau=niveau)