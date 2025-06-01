CREATE TABLE mieszkania
(
    id SERIAL PRIMARY KEY,
    developer TEXT,
    investment TEXT,
    number TEXT,
    area NUMERIC,
    price NUMERIC,
    rooms INTEGER,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION
);