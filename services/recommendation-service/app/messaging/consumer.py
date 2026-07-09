import json
import logging
import threading

import pika

from app.core.config import settings
from app.db.session import SessionLocal
from app.services.gamification_service import handle_quiz_completed, handle_lesson_completed

logger = logging.getLogger(__name__)

QUEUE_NAME = "recommendation-service.gamification"
ROUTING_KEYS = ["quiz.completed", "lesson.completed"]

HANDLERS = {
    "quiz.completed": handle_quiz_completed,
    "lesson.completed": handle_lesson_completed,
}


def _on_message(channel, method, properties, body):
    routing_key = method.routing_key
    handler = HANDLERS.get(routing_key)

    if handler is None:
        logger.warning("Aucun handler pour la routing key '%s', message ignore", routing_key)
        channel.basic_ack(delivery_tag=method.delivery_tag)
        return

    try:
        payload = json.loads(body)
    except json.JSONDecodeError:
        logger.exception("Payload JSON invalide sur '%s', message rejete sans requeue", routing_key)
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
        return

    session = SessionLocal()
    try:
        result = handler(session, payload)
        logger.info("Evenement '%s' traite pour user_id=%s -> %s", routing_key, payload.get("user_id"), result)
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception:
        session.rollback()
        logger.exception("Echec du traitement de '%s', message remis en file", routing_key)
        channel.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
    finally:
        session.close()


def _build_connection() -> pika.BlockingConnection:
    params = pika.URLParameters(settings.RABBITMQ_URL)
    return pika.BlockingConnection(params)


def _run_consumer_loop() -> None:
    connection = _build_connection()
    channel = connection.channel()

    channel.exchange_declare(exchange=settings.RABBITMQ_EXCHANGE, exchange_type="topic", durable=True)
    channel.queue_declare(queue=QUEUE_NAME, durable=True)

    for routing_key in ROUTING_KEYS:
        channel.queue_bind(exchange=settings.RABBITMQ_EXCHANGE, queue=QUEUE_NAME, routing_key=routing_key)

    channel.basic_qos(prefetch_count=10)
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=_on_message)

    logger.info(
        "Consumer gamification demarre sur la queue '%s' (routing keys: %s)",
        QUEUE_NAME, ROUTING_KEYS,
    )

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        channel.stop_consuming()
    finally:
        connection.close()


def start_consumer_in_background() -> threading.Thread:
    """A appeler une seule fois au demarrage de l'application FastAPI."""
    thread = threading.Thread(target=_run_consumer_loop, daemon=True, name="gamification-consumer")
    thread.start()
    return thread
