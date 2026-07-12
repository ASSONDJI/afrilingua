ALTER TABLE answer_attempts ADD COLUMN user_id UUID;

CREATE INDEX idx_answer_attempts_user ON answer_attempts (user_id);
CREATE INDEX idx_answer_attempts_user_question ON answer_attempts (user_id, question_id);
