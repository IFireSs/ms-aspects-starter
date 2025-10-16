CREATE TABLE IF NOT EXISTS error_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    method_signature VARCHAR(512),
    exception_message VARCHAR(512),
    stacktrace VARCHAR(8000),
    params_json VARCHAR(4000),
    service_name VARCHAR(128),
    type VARCHAR(32)
);
