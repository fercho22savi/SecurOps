-- ======================================================================================
-- SECUROPS - ENTERPRISE SECURITY OPERATIONS & PAYROLL SYSTEM
-- PostgreSQL / Standard SQL Relational Schema DDL
-- ======================================================================================

-- 1. SEGURIDAD Y USUARIOS (RBAC)
CREATE TABLE sec_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sec_user_roles (
    user_id BIGINT NOT NULL REFERENCES sec_users(id) ON DELETE CASCADE,
    role_name VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role_name)
);

-- 2. SEDES Y PUESTOS DE CONTROL
CREATE TABLE ops_sites (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(200),
    node_code VARCHAR(50) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE ops_security_posts (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES ops_sites(id) ON DELETE RESTRICT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    coverage_type VARCHAR(30) NOT NULL, -- CONTINUOUS_24_7, DAYTIME_12H, etc.
    requires_firearm BOOLEAN NOT NULL DEFAULT FALSE,
    weapon_serial_number VARCHAR(50),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    geofence_radius_meters INT DEFAULT 50,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. GUARDAS DE SEGURIDAD
CREATE TABLE ops_guards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES sec_users(id) ON DELETE SET NULL,
    national_id VARCHAR(25) NOT NULL UNIQUE,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    professional_license VARCHAR(40) UNIQUE,
    certified_firearms BOOLEAN NOT NULL DEFAULT FALSE,
    firearms_license_expiry DATE,
    status VARCHAR(25) NOT NULL DEFAULT 'ACTIVE',
    performance_score NUMERIC(5, 2) DEFAULT 100.00
);

-- 4. TURNOS Y ESQUEMAS DE ROTACIÓN
CREATE TABLE ops_shift_definitions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE, -- D12, N12, M8, T8, N8, OFF
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_hours INT NOT NULL,
    is_night_shift BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE ops_rotation_schemes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    pattern VARCHAR(30) NOT NULL, -- ROTATION_2X2X2_12H, ROTATION_6X1_8H, etc.
    cycle_days INT NOT NULL,
    required_guards_per_post INT NOT NULL,
    description VARCHAR(250),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE ops_shift_schedules (
    id BIGSERIAL PRIMARY KEY,
    security_post_id BIGINT NOT NULL REFERENCES ops_security_posts(id) ON DELETE RESTRICT,
    guard_id BIGINT NOT NULL REFERENCES ops_guards(id) ON DELETE RESTRICT,
    shift_definition_id BIGINT NOT NULL REFERENCES ops_shift_definitions(id) ON DELETE RESTRICT,
    shift_date DATE NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, UNCOVERED, REPLACED
    relief_guard_id BIGINT REFERENCES ops_guards(id) ON DELETE SET NULL,
    replacement_reason VARCHAR(255),
    operational_notes VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_guard_date_shift UNIQUE (guard_id, shift_date)
);

CREATE INDEX idx_shift_post_date ON ops_shift_schedules(security_post_id, shift_date);
CREATE INDEX idx_shift_guard_date ON ops_shift_schedules(guard_id, shift_date);

-- 5. ASISTENCIA Y MARCACIONES
CREATE TABLE ops_attendance_records (
    id BIGSERIAL PRIMARY KEY,
    shift_schedule_id BIGINT NOT NULL UNIQUE REFERENCES ops_shift_schedules(id) ON DELETE CASCADE,
    guard_id BIGINT NOT NULL REFERENCES ops_guards(id) ON DELETE RESTRICT,
    check_in_time TIMESTAMP WITHOUT TIME ZONE,
    check_out_time TIMESTAMP WITHOUT TIME ZONE,
    delay_minutes INT DEFAULT 0,
    actual_worked_hours NUMERIC(5, 2) DEFAULT 0,
    status VARCHAR(25) NOT NULL DEFAULT 'ON_TIME', -- ON_TIME, DELAYED, EARLY_LEAVE, ABSENT, JUSTIFIED_ABSENCE
    verification_method VARCHAR(30), -- BIOMETRIC_DEVICE, MOBILE_APP_GPS, CONTROL_ROOM_CALL
    check_in_lat NUMERIC(10, 7),
    check_in_lng NUMERIC(10, 7),
    is_geofence_valid BOOLEAN DEFAULT TRUE,
    supervisor_notes VARCHAR(255)
);

CREATE INDEX idx_att_guard_time ON ops_attendance_records(guard_id, check_in_time);

-- 6. LLAMADAS DE CONTROL (RONDA/VERIFICACIÓN) Y NOVEDADES
CREATE TABLE ops_control_call_logs (
    id BIGSERIAL PRIMARY KEY,
    security_post_id BIGINT NOT NULL REFERENCES ops_security_posts(id) ON DELETE RESTRICT,
    guard_id BIGINT NOT NULL REFERENCES ops_guards(id) ON DELETE RESTRICT,
    scheduled_call_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_call_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(25) NOT NULL DEFAULT 'ON_TIME', -- ON_TIME, DELAYED, MISSED, ALERT_RAISED
    operator_user_id BIGINT REFERENCES sec_users(id) ON DELETE SET NULL,
    log_minute_note VARCHAR(500),
    is_synced BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_call_post_time ON ops_control_call_logs(security_post_id, scheduled_call_time);

CREATE TABLE ops_novelty_records (
    id BIGSERIAL PRIMARY KEY,
    guard_id BIGINT NOT NULL REFERENCES ops_guards(id) ON DELETE RESTRICT,
    security_post_id BIGINT REFERENCES ops_security_posts(id) ON DELETE SET NULL,
    type VARCHAR(35) NOT NULL, -- MEDICAL_LEAVE, UNJUSTIFIED_ABSENCE, PERMIT_PAID, etc.
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    impacts_payroll BOOLEAN NOT NULL DEFAULT TRUE,
    payroll_discount_hours INT DEFAULT 0,
    triggers_disciplinary_action BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500) NOT NULL,
    approved_by_user_id BIGINT REFERENCES sec_users(id) ON DELETE SET NULL,
    support_doc_url VARCHAR(300)
);

CREATE INDEX idx_nov_guard_dates ON ops_novelty_records(guard_id, start_date, end_date);

-- 7. NÓMINA Y LIQUIDACIÓN
CREATE TABLE ops_payroll_periods (
    id BIGSERIAL PRIMARY KEY,
    period_code VARCHAR(30) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE ops_payroll_settlements (
    id BIGSERIAL PRIMARY KEY,
    guard_id BIGINT NOT NULL REFERENCES ops_guards(id) ON DELETE RESTRICT,
    payroll_period_id BIGINT NOT NULL REFERENCES ops_payroll_periods(id) ON DELETE RESTRICT,
    regular_day_hours NUMERIC(6, 2) DEFAULT 0,
    night_surcharge_hours NUMERIC(6, 2) DEFAULT 0,
    day_overtime_hours NUMERIC(6, 2) DEFAULT 0,
    night_overtime_hours NUMERIC(6, 2) DEFAULT 0,
    sunday_holiday_day_hours NUMERIC(6, 2) DEFAULT 0,
    sunday_holiday_night_hours NUMERIC(6, 2) DEFAULT 0,
    sunday_holiday_day_overtime_hours NUMERIC(6, 2) DEFAULT 0,
    sunday_holiday_night_overtime_hours NUMERIC(6, 2) DEFAULT 0,
    deducted_absence_hours NUMERIC(6, 2) DEFAULT 0,
    base_salary_amount NUMERIC(12, 2) DEFAULT 0,
    surcharges_and_overtime_amount NUMERIC(12, 2) DEFAULT 0,
    deductions_amount NUMERIC(12, 2) DEFAULT 0,
    net_payable_amount NUMERIC(12, 2) DEFAULT 0,
    calculated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_guard_period UNIQUE (guard_id, payroll_period_id)
);

-- 8. SINCRONIZACIÓN MULTISITIO RESILIENTE (TRANSACTIONAL OUTBOX)
CREATE TABLE ops_sync_outbox (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    origin_node_id VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_attempt_at TIMESTAMP WITHOUT TIME ZONE,
    synced_at TIMESTAMP WITHOUT TIME ZONE,
    retry_count INT DEFAULT 0,
    error_message VARCHAR(500)
);

CREATE INDEX idx_outbox_status_created ON ops_sync_outbox(status, created_at);

CREATE TABLE ops_sync_idempotency (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL UNIQUE,
    origin_node_id VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    response_hash VARCHAR(64)
);
