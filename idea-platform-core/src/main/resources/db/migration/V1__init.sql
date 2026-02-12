-- src/main/resources/db/migration/V1__init.sql

-- Divisions
CREATE TABLE divisions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tribes
CREATE TABLE tribes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    division_id BIGINT NOT NULL REFERENCES divisions(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Teams
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    tribe_id BIGINT NOT NULL REFERENCES tribes(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    team_id BIGINT REFERENCES teams(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Ideas
CREATE TABLE ideas (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    expected_effect TEXT NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    anonymous BOOLEAN NOT NULL DEFAULT false,
    author_id BIGINT REFERENCES users(id),
    division_id BIGINT NOT NULL REFERENCES divisions(id),
    tribe_id BIGINT NOT NULL REFERENCES tribes(id),
    team_id BIGINT NOT NULL REFERENCES teams(id),
    parent_idea_id BIGINT REFERENCES ideas(id),
    review_deadline TIMESTAMP,
    jira_link VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Attachments
CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Comments
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    text TEXT NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Votes
CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    vote_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(idea_id, user_id)
);

-- Review Assignments
CREATE TABLE review_assignments (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    decision VARCHAR(50),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Status History
CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES users(id),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Achievements
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    hero_type VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    icon_path VARCHAR(500),
    threshold INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- User Achievements
CREATE TABLE user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    achievement_id BIGINT NOT NULL REFERENCES achievements(id),
    earned_at TIMESTAMP NOT NULL,
    related_idea_id BIGINT REFERENCES ideas(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, achievement_id)
);

-- Indexes
CREATE INDEX idx_ideas_status ON ideas(status);
CREATE INDEX idx_ideas_type ON ideas(type);
CREATE INDEX idx_ideas_author ON ideas(author_id);
CREATE INDEX idx_ideas_team ON ideas(team_id);
CREATE INDEX idx_votes_idea ON votes(idea_id);
CREATE INDEX idx_votes_user ON votes(user_id);
CREATE INDEX idx_comments_idea ON comments(idea_id);
CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
