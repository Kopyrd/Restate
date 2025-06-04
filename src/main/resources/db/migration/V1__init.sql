CREATE TABLE users (
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

CREATE TABLE mieszkania (
    id SERIAL PRIMARY KEY,
    developer TEXT,
    investment TEXT,
    number TEXT,
    area NUMERIC(10,2),
    price NUMERIC(12,2),
    voivodeship TEXT,
    city TEXT,
    district TEXT,
    floor INTEGER,
    -- Dodatkowe pola funkcjonalne
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);