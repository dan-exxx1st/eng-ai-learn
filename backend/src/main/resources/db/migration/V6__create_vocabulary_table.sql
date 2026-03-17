CREATE TABLE user_vocabulary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    word VARCHAR(255) NOT NULL,
    translation VARCHAR(255),
    context TEXT,
    source VARCHAR(50),
    learned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, word)
);

CREATE INDEX idx_vocabulary_user ON user_vocabulary(user_id);
CREATE INDEX idx_vocabulary_learned ON user_vocabulary(user_id, learned);
