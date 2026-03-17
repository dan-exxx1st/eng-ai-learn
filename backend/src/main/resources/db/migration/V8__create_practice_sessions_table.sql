CREATE TABLE practice_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scenario_type VARCHAR(50) NOT NULL,
    messages JSONB NOT NULL DEFAULT '[]',
    feedback TEXT,
    score INTEGER,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);
CREATE INDEX idx_practice_user ON practice_sessions(user_id);
