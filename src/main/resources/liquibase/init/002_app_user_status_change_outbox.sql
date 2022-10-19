CREATE TABLE app_user_status_change_outbox
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES app_user (id),
    status            TEXT        NOT NULL,
    modified_by       TEXT        NOT NULL,
    created_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
