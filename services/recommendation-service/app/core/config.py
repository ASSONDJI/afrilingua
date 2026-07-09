import os
from dotenv import load_dotenv
load_dotenv()
class Settings:
    SERVICE_NAME: str = "recommendation-service"
    SERVICE_PORT: int = int(os.getenv("SERVICE_PORT", "8086"))
    EUREKA_SERVER: str = os.getenv("EUREKA_SERVER", "http://localhost:8761/eureka")
    # Host advertised to Eureka so the gateway can reach this instance
    INSTANCE_HOST: str = os.getenv("INSTANCE_HOST", "localhost")

    # Database (progressions, badges, user_badges -- gamification data)
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "postgresql+psycopg2://afrilingua:afrilingua_dev_password@localhost:5438/afrilingua_recommendation",
    )

    # RabbitMQ (consumes quiz.completed / lesson.completed events)
    RABBITMQ_URL: str = os.getenv(
        "RABBITMQ_URL",
        "amqp://afrilingua:afrilingua_dev_password@localhost:5672/",
    )
    RABBITMQ_EXCHANGE: str = os.getenv("RABBITMQ_EXCHANGE", "afrilingua.events")
settings = Settings()
