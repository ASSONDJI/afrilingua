from datetime import datetime

from pydantic import BaseModel, Field


class WordFeatures(BaseModel):
    """One word's features as needed by clustering/difficulty/PCA — mirrors
    the shape content-service exposes, so this service stays a pure
    consumer, never a source of truth for word data (database-per-service)."""
    word_id: str
    nb_syllabes: int
    longueur_mot: int
    ton1: str = Field(description="one of: bas, moyen, haut")
    ton2: str = Field(description="one of: bas, moyen, haut")


class ClusterRequest(BaseModel):
    words: list[WordFeatures]
    n_clusters: int = 8


class ClusterResponse(BaseModel):
    word_id: str
    cluster: int


class DifficultyTrainRequest(BaseModel):
    words: list[WordFeatures]


class DifficultyTrainResponse(BaseModel):
    accuracy_on_training_rule: float
    human_readable_rules: str
    depth: int
    n_leaves: int
    note: str = (
        "This measures how well the tree reproduces the hand-written "
        "tone-based labeling rule, not predictive accuracy on unseen "
        "learner outcomes. See module docstring for details."
    )


class DifficultyClassifyRequest(BaseModel):
    nb_syllabes: int
    longueur_mot: int
    ton1: str
    ton2: str


class DifficultyClassifyResponse(BaseModel):
    niveau: str


class PcaRequest(BaseModel):
    words: list[WordFeatures]


class PcaPoint(BaseModel):
    word_id: str
    x: float
    y: float


class PcaResponse(BaseModel):
    points: list[PcaPoint]
    explained_variance_ratio: list[float]


class ReviewStateModel(BaseModel):
    repetitions: int = 0
    ease_factor: float = 2.5
    interval_days: int = 0


class SrsReviewRequest(BaseModel):
    state: ReviewStateModel
    quality: int = Field(ge=0, le=5)


class SrsReviewResponse(BaseModel):
    next_review_at: datetime
    new_state: ReviewStateModel
