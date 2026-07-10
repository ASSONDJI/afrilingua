#!/bin/bash
set -e

ROOT="$HOME/afrilingua"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

echo "=== 1. Bases de donnees et RabbitMQ (Docker) ==="
cd "$ROOT"
docker compose up -d

echo "=== 2. Discovery server (Eureka) ==="
cd "$ROOT/services/discovery-server"
nohup mvn spring-boot:run > "$LOG_DIR/discovery-server.log" 2>&1 &
echo "  PID $! -- attente du demarrage (30s)..."
sleep 30

echo "=== 3. API Gateway ==="
cd "$ROOT/services/api-gateway"
nohup mvn spring-boot:run > "$LOG_DIR/api-gateway.log" 2>&1 &
echo "  PID $!"

echo "=== 4. Services metier Java (en parallele) ==="
for service in auth-service user-service content-service lesson-service quiz-service; do
  cd "$ROOT/services/$service"
  nohup mvn spring-boot:run > "$LOG_DIR/$service.log" 2>&1 &
  echo "  $service -- PID $!"
done

echo "=== 5. Recommendation service (Python/FastAPI) ==="
cd "$ROOT/services/recommendation-service"
source venv/bin/activate
nohup uvicorn app.main:app --port 8086 > "$LOG_DIR/recommendation-service.log" 2>&1 &
echo "  PID $!"

echo ""
echo "Tous les services sont lances en arriere-plan."
echo "Logs disponibles dans $LOG_DIR/"
echo "Attends ~30-45s de plus que tout s'enregistre aupres d'Eureka avant de tester l'app."
echo "Verifie l'etat sur http://localhost:8761 (dashboard Eureka)."
