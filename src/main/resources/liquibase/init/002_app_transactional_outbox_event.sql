CREATE TABLE IF NOT EXISTS app_transactional_outbox_event
(
    id                BIGSERIAL PRIMARY KEY,
    topic             TEXT NOT NULL,
    key               TEXT,
    event             TEXT NOT NULL,
    created_timestamp TIMESTAMPTZ DEFAULT NULL
);
