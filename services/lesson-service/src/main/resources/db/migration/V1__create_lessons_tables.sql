CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE lessons (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_id   UUID NOT NULL,
    title         VARCHAR(255) NOT NULL,
    lesson_order  INT NOT NULL,
    level         INT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE lesson_words (
    lesson_id  UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    word_id    UUID NOT NULL,
    PRIMARY KEY (lesson_id, word_id)
);

CREATE INDEX idx_lessons_language ON lessons (language_id);
