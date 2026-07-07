CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id       UUID NOT NULL,
    type            VARCHAR(30) NOT NULL,
    question_text   TEXT NOT NULL,
    correct_answer  VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE question_options (
    question_id  UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text  VARCHAR(255) NOT NULL
);

CREATE TABLE answer_attempts (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id       UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    submitted_answer  VARCHAR(255) NOT NULL,
    is_correct        BOOLEAN NOT NULL,
    submitted_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_questions_lesson ON questions (lesson_id);
CREATE INDEX idx_answer_attempts_question ON answer_attempts (question_id);
