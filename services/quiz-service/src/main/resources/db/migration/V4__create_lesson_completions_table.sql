CREATE TABLE lesson_completions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL,
    lesson_id     UUID NOT NULL,
    completed_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, lesson_id)
);

CREATE INDEX idx_lesson_completions_user ON lesson_completions (user_id);
