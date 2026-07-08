import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    SERVICE_NAME: str = "recommendation-service"
    SERVICE_PORT: int = int(os.getenv("SERVICE_PORT", "8086"))
    EUREKA_SERVER: str = os.getenv("EUREKA_SERVER", "http://localhost:8761/eureka")
    # Host advertised to Eureka so the gateway can reach this instance
    INSTANCE_HOST: str = os.getenv("INSTANCE_HOST", "localhost")


settings = Settings()
