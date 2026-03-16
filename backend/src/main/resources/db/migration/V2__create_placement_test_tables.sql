CREATE TABLE placement_test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    questions JSONB NOT NULL,
    answers JSONB,
    determined_level VARCHAR(20),
    score INTEGER,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_placement_test_user ON placement_test_results(user_id);
