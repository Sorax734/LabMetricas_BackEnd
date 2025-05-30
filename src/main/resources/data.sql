-- Roles
INSERT INTO roles (name, created_at, updated_at) VALUES
('ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUPERVISOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('OPERADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- Users
-- Note: Passwords are hashed versions of the actual passwords (admin123, super123, oper123)
INSERT INTO users (id, name, lastname, email, password, status, enabled, role_id, company_name, created_at, updated_at) VALUES
-- Admins
(UUID(), 'Admin', 'System', 'admin1@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'ADMIN'), 'Company A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Admin', 'Manager', 'admin2@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'ADMIN'), 'Company B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Supervisors
(UUID(), 'Super', 'Visor', 'supervisor1@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'SUPERVISOR'), 'Company A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Super', 'Manager', 'supervisor2@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'SUPERVISOR'), 'Company B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Super', 'Lead', 'supervisor3@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'SUPERVISOR'), 'Company C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Operators
(UUID(), 'Oper', 'One', 'operator1@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'OPERADOR'), 'Company A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Oper', 'Two', 'operator2@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'OPERADOR'), 'Company B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Oper', 'Three', 'operator3@labmetricas.com', '$2a$10$YourHashedPasswordHere', 1, 1, (SELECT id FROM roles WHERE name = 'OPERADOR'), 'Company C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at); 