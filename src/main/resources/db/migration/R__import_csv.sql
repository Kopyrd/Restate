-- Usuń poprzednie dane
TRUNCATE TABLE mieszkania RESTART IDENTITY CASCADE;

CREATE TEMP TABLE temp_csv (
    id INTEGER,
    developer TEXT,
    investment TEXT,
    number TEXT,
    area NUMERIC(10,2),
    price NUMERIC(12,2),
    country TEXT,
    voivodeship TEXT,
    county TEXT,
    city TEXT,
    district TEXT,
    lat NUMERIC,
    lng NUMERIC,
    floor INTEGER,
    floors INTEGER,
    balcony NUMERIC,
    loggia NUMERIC,
    terrace NUMERIC,
    garden NUMERIC
);

COPY temp_csv FROM '/data/mieszkania.csv'
WITH (FORMAT csv, HEADER true);

INSERT INTO mieszkania (id, developer, investment, number, area, price, voivodeship, city, district, floor)
SELECT id, developer, investment, number, area, price, voivodeship, city, district, floor
FROM temp_csv;

DROP TABLE temp_csv;

SELECT setval(
    pg_get_serial_sequence('mieszkania','id'),
    (SELECT MAX(id) FROM mieszkania),
    true
);