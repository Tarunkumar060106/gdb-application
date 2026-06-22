CREATE TABLE IF NOT EXISTS security_alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    message VARCHAR(500) NOT NULL,
    alert_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_security_alerts_user_id ON security_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_security_alerts_date ON security_alerts(alert_date);
