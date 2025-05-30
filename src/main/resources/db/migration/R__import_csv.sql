COPY mieszkania(id, developer, investment, number, area, price, rooms, lat, lng)
    FROM '/data/mieszkania.csv'
    WITH (FORMAT csv, HEADER true);
