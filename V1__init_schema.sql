-- EmployeeHub Initial Schema
-- Timezone strategy: All timestamps stored in UTC. Application converts to user locale as needed.

CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id       BIGINT NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role_id);

CREATE TABLE departments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    code        VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(500),
    head_id     BIGINT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE employees (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code      VARCHAR(30) NOT NULL UNIQUE,
    user_id            BIGINT NULL UNIQUE,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    email              VARCHAR(255) NOT NULL UNIQUE,
    phone              VARCHAR(30),
    date_of_birth      DATE,
    gender             VARCHAR(20),
    address            VARCHAR(500),
    city               VARCHAR(100),
    state              VARCHAR(100),
    country            VARCHAR(100),
    postal_code        VARCHAR(20),
    department_id      BIGINT,
    designation        VARCHAR(150),
    employment_status  VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    joining_date       DATE NOT NULL,
    resignation_date   DATE NULL,
    salary             DECIMAL(15, 2),
    emergency_contact_name  VARCHAR(150),
    emergency_contact_phone VARCHAR(30),
    profile_image_url  VARCHAR(500),
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMP NULL,
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(employment_status);
CREATE INDEX idx_employees_name ON employees(last_name, first_name);

ALTER TABLE departments
    ADD CONSTRAINT fk_departments_head FOREIGN KEY (head_id) REFERENCES employees(id);

CREATE TABLE attendance_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_at     TIMESTAMP NULL,
    check_out_at    TIMESTAMP NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    work_hours      DECIMAL(5, 2),
    notes           VARCHAR(500),
    corrected_by    BIGINT NULL,
    corrected_at    TIMESTAMP NULL,
    correction_reason VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_attendance_corrected_by FOREIGN KEY (corrected_by) REFERENCES users(id),
    CONSTRAINT uk_attendance_employee_date UNIQUE (employee_id, attendance_date)
);

CREATE INDEX idx_attendance_date ON attendance_records(attendance_date);
CREATE INDEX idx_attendance_employee ON attendance_records(employee_id);
CREATE INDEX idx_attendance_status ON attendance_records(status);

CREATE TABLE leave_balances (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    leave_type      VARCHAR(30) NOT NULL,
    year            INT NOT NULL,
    total_days      DECIMAL(5, 1) NOT NULL DEFAULT 0,
    used_days       DECIMAL(5, 1) NOT NULL DEFAULT 0,
    pending_days    DECIMAL(5, 1) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uk_leave_balance UNIQUE (employee_id, leave_type, year)
);

CREATE TABLE leave_requests (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    leave_type      VARCHAR(30) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    total_days      DECIMAL(5, 1) NOT NULL,
    reason          VARCHAR(1000) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by     BIGINT NULL,
    reviewed_at     TIMESTAMP NULL,
    review_comments VARCHAR(1000),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_request_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_leave_request_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates ON leave_requests(start_date, end_date);

CREATE TABLE leave_approval_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_request_id BIGINT NOT NULL,
    action          VARCHAR(30) NOT NULL,
    performed_by    BIGINT NOT NULL,
    comments        VARCHAR(1000),
    performed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_history_request FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id),
    CONSTRAINT fk_leave_history_user FOREIGN KEY (performed_by) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     BIGINT,
    action        VARCHAR(50) NOT NULL,
    performed_by  BIGINT,
    details       TEXT,
    ip_address    VARCHAR(45),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (performed_by) REFERENCES users(id)
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- Seed roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Full system administration'),
('HR', 'Human Resources - employee, department, attendance and leave administration'),
('EMPLOYEE', 'Standard employee access to personal profile, attendance and leave');
