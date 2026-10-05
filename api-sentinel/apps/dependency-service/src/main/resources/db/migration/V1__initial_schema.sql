CREATE TABLE IF NOT EXISTS api_consumer (
    id             UUID PRIMARY KEY,
    service_name   VARCHAR(255) NOT NULL,
    api_name       VARCHAR(255) NOT NULL,
    api_version    VARCHAR(64)  NOT NULL,
    contact_owner  VARCHAR(255),
    environment    VARCHAR(32)  NOT NULL DEFAULT 'prod',
    created_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_consumer_svc_api_ver UNIQUE (service_name, api_name, api_version)
);

CREATE INDEX IF NOT EXISTS idx_consumer_api  ON api_consumer(api_name);
CREATE INDEX IF NOT EXISTS idx_consumer_svc  ON api_consumer(service_name);