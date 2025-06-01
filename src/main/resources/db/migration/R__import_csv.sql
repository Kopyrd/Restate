COPY mieszkania(id, developer, investment, number, area, price, rooms, lat, lng)
    FROM '/data/mieszkania.csv'
    WITH (FORMAT csv, HEADER true);

SELECT setval(
               pg_get_serial_sequence('mieszkania','id'),
               (SELECT MAX(id) FROM mieszkania),
               true
       );