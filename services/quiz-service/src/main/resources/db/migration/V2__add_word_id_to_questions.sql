ALTER TABLE questions ADD COLUMN word_id UUID;

CREATE INDEX idx_questions_word ON questions (word_id);
