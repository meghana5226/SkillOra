-- ===================================================================
-- Skillora initial schema
-- ===================================================================

CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(120)  NOT NULL,
    email               VARCHAR(180)  NOT NULL UNIQUE,
    password            VARCHAR(255)  NOT NULL,
    profile_image       VARCHAR(500),
    bio                 VARCHAR(1000),
    location            VARCHAR(150),
    timezone            VARCHAR(80),
    experience_level    VARCHAR(30)   NOT NULL DEFAULT 'BEGINNER',
    availability        VARCHAR(500),
    role                VARCHAR(20)   NOT NULL DEFAULT 'USER',
    points              INTEGER       NOT NULL DEFAULT 0,
    level               INTEGER       NOT NULL DEFAULT 1,
    reputation_score    NUMERIC(5,2)  NOT NULL DEFAULT 0,
    onboarding_complete BOOLEAN       NOT NULL DEFAULT FALSE,
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

CREATE TABLE skills (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    category    VARCHAR(80)  NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_skills_category ON skills(category);

CREATE TABLE user_offered_skills (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id           BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    proficiency_level  VARCHAR(30) NOT NULL DEFAULT 'BEGINNER',
    years_experience   INTEGER NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, skill_id)
);
CREATE INDEX idx_offered_user ON user_offered_skills(user_id);
CREATE INDEX idx_offered_skill ON user_offered_skills(skill_id);

CREATE TABLE user_wanted_skills (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id      BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    target_level  VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
    priority      INTEGER NOT NULL DEFAULT 1,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, skill_id)
);
CREATE INDEX idx_wanted_user ON user_wanted_skills(user_id);
CREATE INDEX idx_wanted_skill ON user_wanted_skills(skill_id);

CREATE TABLE swap_requests (
    id                BIGSERIAL PRIMARY KEY,
    sender_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    offered_skill_id  BIGINT NOT NULL REFERENCES skills(id),
    requested_skill_id BIGINT NOT NULL REFERENCES skills(id),
    message           VARCHAR(1000),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (sender_id <> receiver_id)
);
CREATE INDEX idx_swap_sender ON swap_requests(sender_id);
CREATE INDEX idx_swap_receiver ON swap_requests(receiver_id);
CREATE INDEX idx_swap_status ON swap_requests(status);

CREATE TABLE conversations (
    id              BIGSERIAL PRIMARY KEY,
    participant1_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    participant2_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(participant1_id, participant2_id)
);
CREATE INDEX idx_conv_p1 ON conversations(participant1_id);
CREATE INDEX idx_conv_p2 ON conversations(participant2_id);

CREATE TABLE messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         VARCHAR(2000) NOT NULL,
    sent_at         TIMESTAMP NOT NULL DEFAULT now(),
    read            BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_msg_conversation ON messages(conversation_id);

CREATE TABLE sessions (
    id             BIGSERIAL PRIMARY KEY,
    requester_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id       BIGINT NOT NULL REFERENCES skills(id),
    scheduled_date DATE NOT NULL,
    start_time     TIME NOT NULL,
    end_time       TIME NOT NULL,
    meeting_link   VARCHAR(500),
    status         VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    notes          VARCHAR(1000),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (end_time > start_time)
);
CREATE INDEX idx_session_requester ON sessions(requester_id);
CREATE INDEX idx_session_receiver ON sessions(receiver_id);
CREATE INDEX idx_session_status ON sessions(status);

CREATE TABLE reviews (
    id               BIGSERIAL PRIMARY KEY,
    reviewer_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewed_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id       BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    rating           INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment          VARCHAR(1000),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(session_id, reviewer_id)
);
CREATE INDEX idx_review_reviewed_user ON reviews(reviewed_user_id);

CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(50) NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    VARCHAR(1000) NOT NULL,
    read       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_read ON notifications(read);

CREATE TABLE learning_goals (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    skill_id    BIGINT REFERENCES skills(id),
    target_date DATE,
    progress    INTEGER NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
    status      VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_goal_user ON learning_goals(user_id);

CREATE TABLE admin_audit (
    id          BIGSERIAL PRIMARY KEY,
    admin_id    BIGINT NOT NULL REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id   BIGINT,
    details     VARCHAR(1000),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);
