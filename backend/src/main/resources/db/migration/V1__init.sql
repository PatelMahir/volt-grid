CREATE TABLE stations (
    id             UUID PRIMARY KEY,
    external_id    VARCHAR(128) NOT NULL UNIQUE,
    name           VARCHAR(200) NOT NULL,
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    connector_type VARCHAR(16),
    power_kw       DOUBLE PRECISION,
    price_per_kwh  NUMERIC(8, 4),
    status         VARCHAR(16)  NOT NULL,
    last_seen_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE charging_sessions (
    id                  UUID PRIMARY KEY,
    station_external_id VARCHAR(128) NOT NULL,
    energy_kwh          DOUBLE PRECISION NOT NULL,
    cost                NUMERIC(10, 2),
    status              VARCHAR(16)  NOT NULL,
    started_at          TIMESTAMPTZ  NOT NULL,
    ended_at            TIMESTAMPTZ
);

CREATE INDEX idx_session_station_time
    ON charging_sessions (station_external_id, started_at DESC);
