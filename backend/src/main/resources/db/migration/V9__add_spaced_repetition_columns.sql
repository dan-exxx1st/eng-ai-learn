ALTER TABLE user_vocabulary ADD COLUMN ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5;
ALTER TABLE user_vocabulary ADD COLUMN interval_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE user_vocabulary ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0;
ALTER TABLE user_vocabulary ADD COLUMN next_review_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE INDEX idx_vocabulary_next_review ON user_vocabulary(user_id, next_review_at);
