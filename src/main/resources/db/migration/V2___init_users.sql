-- Dodanie tabeli users
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Dodanie kolumny created_by do tabeli mieszkania
ALTER TABLE mieszkania
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'AVAILABLE',
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(id);

-- Wstawienie przykładowych użytkowników
-- Hasło dla obu użytkowników: password123 (zahashowane BCrypt)
INSERT INTO users (username, password, email, first_name, last_name, role) VALUES
                                                                               ('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'admin@restate.pl', 'Admin', 'User', 'ADMIN'),
                                                                               ('user1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'user1@restate.pl', 'John', 'Doe', 'USER')
    ON CONFLICT (username) DO NOTHING;