from fastapi import APIRouter

from app.api.clustering import router as clustering_router
from app.api.difficulty import router as difficulty_router
from app.api.pca import router as pca_router
from app.api.srs import router as srs_router
from app.api.progression import router as progression_router

router = APIRouter()
router.include_router(clustering_router, prefix="/clustering", tags=["clustering"])
router.include_router(difficulty_router, prefix="/difficulty", tags=["difficulty"])
router.include_router(pca_router, prefix="/pca", tags=["pca"])
router.include_router(srs_router, prefix="/srs", tags=["srs"])
router.include_router(progression_router, prefix="/progressions", tags=["progression"])
