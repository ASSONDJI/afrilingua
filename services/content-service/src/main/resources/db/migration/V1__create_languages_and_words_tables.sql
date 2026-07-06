CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE languages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(10) NOT NULL UNIQUE,
    region      VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE words (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_id            UUID NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    word                   VARCHAR(255) NOT NULL,
    translation            VARCHAR(255) NOT NULL,
    grammatical_category   VARCHAR(50),
    phonetic_ipa           VARCHAR(255),
    audio_url              VARCHAR(500),
    difficulty_level       VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_words_language ON words (language_id);
