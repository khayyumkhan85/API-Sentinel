CREATE TABLE IF NOT EXISTS api_contract (
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    version        VARCHAR(64)  NOT NULL,
    owner          VARCHAR(255),
    repository     VARCHAR(512),
    status         VARCHAR(32)  NOT NULL,
    specification  TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_contract_name_version UNIQUE (name, version)
);

CREATE INDEX IF NOT EXISTS idx_contract_name ON api_contract(name);
CREATE INDEX IF NOT EXISTS idx_contract_status ON api_contract(status);

CREATE TABLE IF NOT EXISTS contract_change (
    id              UUID PRIMARY KEY,
    contract_id     UUID         NOT NULL,
    from_version    VARCHAR(64),
    to_version      VARCHAR(64),
    severity        VARCHAR(32)  NOT NULL,
    breaking        BOOLEAN      NOT NULL,
    path            VARCHAR(512),
    operation       VARCHAR(16),
    change_type     VARCHAR(64)  NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_change_contract ON contract_change(contract_id);

CREATE TABLE IF NOT EXISTS impact_assessment (
    id                   UUID PRIMARY KEY,
    change_id            UUID         NOT NULL,
    risk_score           INT          NOT NULL,
    affected_consumers   INT          NOT NULL,
    business_impact      TEXT,
    recommendation       TEXT,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);