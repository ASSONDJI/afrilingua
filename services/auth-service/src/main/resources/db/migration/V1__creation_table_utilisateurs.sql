CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE utilisateurs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    date_creation   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_utilisateurs_email ON utilisateurs (email);
