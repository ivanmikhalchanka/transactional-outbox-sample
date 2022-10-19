CREATE TABLE app_user
(
    id                BIGSERIAL PRIMARY KEY,
    email             TEXT        NOT NULL,
    status            TEXT CHECK ( status IN ('REGISTERED', 'ACTIVE', 'SUSPENDED') ) DEFAULT 'REGISTERED',
    created_timestamp TIMESTAMPTZ NOT NULL                                           DEFAULT CURRENT_TIMESTAMP
);
