-- Roles
INSERT INTO roles (name, created_at, updated_at) VALUES
('ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUPERVISOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('OPERADOR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- Users
-- Note: Passwords are hashed versions of the actual passwords (admin123, super123, oper123)
INSERT INTO users (id, name, email, password, status, enabled, role_id, created_at, updated_at) VALUES
    (UUID_TO_BIN(UUID()), 'Admin User', 'admin@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 1, 1, (SELECT id FROM roles WHERE name = 'ADMIN'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'Supervisor User', 'supervisor@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 1, 1, (SELECT id FROM roles WHERE name = 'SUPERVISOR'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'Operator User', 'operator@example.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 1, 1, (SELECT id FROM roles WHERE name = 'OPERADOR'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 