CREATE TABLE learning_programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    language_level VARCHAR(20) NOT NULL,
    specialization VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID NOT NULL REFERENCES learning_programs(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    lesson_type VARCHAR(30) NOT NULL,
    content JSONB,
    order_index INTEGER NOT NULL,
    is_generated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    exercise_type VARCHAR(30) NOT NULL,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    question TEXT NOT NULL,
    options JSONB,
    correct_answer TEXT,
    explanation TEXT,
    order_index INTEGER NOT NULL
);

CREATE INDEX idx_programs_user ON learning_programs(user_id);
CREATE INDEX idx_modules_program ON modules(program_id);
CREATE INDEX idx_lessons_module ON lessons(module_id);
CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
