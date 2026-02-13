CREATE TABLE IF NOT EXISTS system_settings (
    id              BIGSERIAL PRIMARY KEY,
    setting_key     VARCHAR(255) NOT NULL UNIQUE,
    setting_value   TEXT,
    description     VARCHAR(500),
    setting_group   VARCHAR(100) NOT NULL DEFAULT 'general',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX idx_system_settings_group ON system_settings(setting_group);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
