from contextlib import asynccontextmanager

import py_eureka_client.eureka_client as eureka_client
from fastapi import FastAPI

from app.core.config import settings
from app.api import router as api_router
from app.messaging.consumer import start_consumer_in_background


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Register with Eureka on startup, exactly like every Spring Boot service
    # in this project, so api-gateway can route /api/recommendations/** here.
    await eureka_client.init_async(
        eureka_server=settings.EUREKA_SERVER,
        app_name=settings.SERVICE_NAME.upper(),
        instance_host=settings.INSTANCE_HOST,
        instance_port=settings.SERVICE_PORT,
    )
    # Consumer RabbitMQ (quiz.completed / lesson.completed -> gamification)
    # tourne dans un thread daemon separe, car pika est bloquant et
    # incompatible avec la boucle asyncio de FastAPI/uvicorn.
    start_consumer_in_background()
    yield
    await eureka_client.stop_async()


app = FastAPI(
    title="AfriLingua Recommendation Service",
    description="Word clustering, difficulty rule extraction, PCA diagnostics, and spaced repetition.",
    version="1.0.0",
    lifespan=lifespan,
)
app.include_router(api_router, prefix="/api/recommendations")


@app.get("/actuator/health")
def health():
    # Named to match the Spring Boot services' health endpoint convention,
    # even though this is FastAPI — keeps monitoring/tooling consistent.
    return {"status": "UP"}
