CREATE TABLE tasks_tracker.revoked_tokens
(
    jti        VARCHAR(36) PRIMARY KEY,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_revoked_tokens_expires_at
    ON tasks_tracker.revoked_tokens (expires_at);