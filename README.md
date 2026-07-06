# AfriLingua

Application mobile d'apprentissage des langues maternelles camerounaises (Yemba, Duala, Bassa),
architecturée en microservices.

## Structure
- `services/` — microservices backend (Spring Boot pour le cœur métier, Python/FastAPI pour l'IA)
- `mobile/` — application Flutter
- `infra/` — Docker Compose, manifestes Kubernetes
- `docs/` — cahier des charges et documentation

## Démarrage local
Voir `docker-compose.yml` à la racine : `docker compose up -d`

## Cahier des charges
Voir `docs/AfriLingua_Cahier_des_Charges.docx`