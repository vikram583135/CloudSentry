-- Database Initialization Script
-- This script runs automatically when PostgreSQL container starts for the first time

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =============================================
-- USERS & AUTHENTICATION
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'SRE', 'DEVELOPER', 'MANAGER')),
    UNIQUE(user_id, role)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- =============================================
-- APPLICATIONS
-- =============================================

CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    environment VARCHAR(50) DEFAULT 'production',
    owner_id UUID REFERENCES users(id),
    metadata JSONB DEFAULT '{}',
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_applications_name ON applications(name);
CREATE INDEX idx_applications_status ON applications(status);

-- =============================================
-- METRICS
-- =============================================

CREATE TABLE IF NOT EXISTS metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    metric_type VARCHAR(100) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50),
    labels JSONB DEFAULT '{}',
    collected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Partition metrics by time for better performance (monthly partitions)
CREATE INDEX idx_metrics_app_id ON metrics(app_id);
CREATE INDEX idx_metrics_collected_at ON metrics(collected_at DESC);
CREATE INDEX idx_metrics_type_name ON metrics(metric_type, metric_name);
CREATE INDEX idx_metrics_app_time ON metrics(app_id, collected_at DESC);

-- =============================================
-- THRESHOLD CONFIGURATIONS
-- =============================================

CREATE TABLE IF NOT EXISTS threshold_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID REFERENCES applications(id) ON DELETE CASCADE,
    metric_type VARCHAR(100) NOT NULL,
    warning_threshold DOUBLE PRECISION,
    critical_threshold DOUBLE PRECISION,
    comparison_operator VARCHAR(20) DEFAULT 'GREATER_THAN',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(app_id, metric_type)
);

-- =============================================
-- ANOMALIES
-- =============================================

CREATE TABLE IF NOT EXISTS anomalies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    metric_id UUID REFERENCES metrics(id),
    anomaly_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    current_value DOUBLE PRECISION NOT NULL,
    expected_value DOUBLE PRECISION,
    deviation_percent DOUBLE PRECISION,
    description TEXT,
    detected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) DEFAULT 'OPEN'
);

CREATE INDEX idx_anomalies_app_id ON anomalies(app_id);
CREATE INDEX idx_anomalies_detected_at ON anomalies(detected_at DESC);
CREATE INDEX idx_anomalies_status ON anomalies(status);

-- =============================================
-- INCIDENTS
-- =============================================

CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    severity VARCHAR(10) NOT NULL CHECK (severity IN ('P1', 'P2', 'P3', 'P4')),
    status VARCHAR(50) NOT NULL DEFAULT 'DETECTED' CHECK (status IN ('DETECTED', 'INVESTIGATING', 'MITIGATED', 'RESOLVED')),
    source VARCHAR(100) DEFAULT 'AUTOMATIC',
    assignee_id UUID REFERENCES users(id),
    root_cause_suggestions JSONB DEFAULT '[]',
    resolution_notes TEXT,
    detected_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    mitigated_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incidents_app_id ON incidents(app_id);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_detected_at ON incidents(detected_at DESC);

-- =============================================
-- INCIDENT TIMELINE
-- =============================================

CREATE TABLE IF NOT EXISTS incident_timeline (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    actor_id UUID REFERENCES users(id),
    actor_name VARCHAR(255),
    notes TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incident_timeline_incident_id ON incident_timeline(incident_id);

-- =============================================
-- COST RECORDS
-- =============================================

CREATE TABLE IF NOT EXISTS cost_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_name VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255),
    resource_type VARCHAR(100),
    cost_amount DECIMAL(15, 6) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    usage_quantity DOUBLE PRECISION,
    usage_unit VARCHAR(50),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    account_id VARCHAR(100),
    region VARCHAR(50),
    tags JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cost_records_service ON cost_records(service_name);
CREATE INDEX idx_cost_records_period ON cost_records(period_start, period_end);
CREATE INDEX idx_cost_records_resource ON cost_records(resource_id);

-- =============================================
-- COST OPTIMIZATION SUGGESTIONS
-- =============================================

CREATE TABLE IF NOT EXISTS cost_suggestions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resource_id VARCHAR(255),
    resource_type VARCHAR(100),
    suggestion_type VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    potential_savings DECIMAL(15, 2),
    currency VARCHAR(10) DEFAULT 'USD',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'OPEN',
    implemented_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cost_suggestions_status ON cost_suggestions(status);
CREATE INDEX idx_cost_suggestions_type ON cost_suggestions(suggestion_type);

-- =============================================
-- ALERTS & NOTIFICATIONS
-- =============================================

CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE,
    anomaly_id UUID REFERENCES anomalies(id) ON DELETE CASCADE,
    alert_type VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    recipient VARCHAR(255),
    message TEXT,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'SUPPRESSED')),
    retry_count INTEGER DEFAULT 0,
    last_error TEXT,
    scheduled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_incident_id ON alerts(incident_id);
CREATE INDEX idx_alerts_scheduled_at ON alerts(scheduled_at);

-- =============================================
-- NOTIFICATION CHANNELS CONFIGURATION
-- =============================================

CREATE TABLE IF NOT EXISTS notification_channels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    channel_type VARCHAR(50) NOT NULL CHECK (channel_type IN ('EMAIL', 'SLACK', 'PAGERDUTY', 'WEBHOOK', 'TEAMS')),
    config JSONB NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- REFRESH TOKENS (for JWT)
-- =============================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- =============================================
-- AUDIT LOG
-- =============================================

CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);

-- =============================================
-- DEFAULT DATA
-- =============================================

-- Insert default admin user (password: admin123 - BCrypt hashed)
INSERT INTO users (id, email, password_hash, name, enabled)
VALUES (
    uuid_generate_v4(),
    'admin@devops.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rCMsRoSe7EjKkqK1Xy',
    'System Administrator',
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign admin role
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@devops.local'
ON CONFLICT DO NOTHING;

-- Insert default thresholds
INSERT INTO threshold_configs (metric_type, warning_threshold, critical_threshold, comparison_operator)
VALUES
    ('CPU_USAGE', 70, 90, 'GREATER_THAN'),
    ('MEMORY_USAGE', 75, 95, 'GREATER_THAN'),
    ('DISK_USAGE', 80, 95, 'GREATER_THAN'),
    ('LATENCY_P99', 500, 1000, 'GREATER_THAN'),
    ('ERROR_RATE', 1, 5, 'GREATER_THAN'),
    ('REQUEST_RATE', NULL, NULL, 'ANOMALY')
ON CONFLICT DO NOTHING;

-- =============================================
-- FUNCTIONS & TRIGGERS
-- =============================================

-- Function to update 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to relevant tables
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_applications_updated_at
    BEFORE UPDATE ON applications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_incidents_updated_at
    BEFORE UPDATE ON incidents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_threshold_configs_updated_at
    BEFORE UPDATE ON threshold_configs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- VIEWS
-- =============================================

-- Active incidents view
CREATE OR REPLACE VIEW active_incidents AS
SELECT 
    i.*,
    a.name as app_name,
    u.name as assignee_name,
    (SELECT COUNT(*) FROM incident_timeline t WHERE t.incident_id = i.id) as timeline_count
FROM incidents i
LEFT JOIN applications a ON i.app_id = a.id
LEFT JOIN users u ON i.assignee_id = u.id
WHERE i.status NOT IN ('RESOLVED');

-- Daily cost summary view
CREATE OR REPLACE VIEW daily_cost_summary AS
SELECT 
    period_start as date,
    service_name,
    SUM(cost_amount) as total_cost,
    COUNT(*) as record_count
FROM cost_records
GROUP BY period_start, service_name
ORDER BY period_start DESC, total_cost DESC;

COMMIT;
