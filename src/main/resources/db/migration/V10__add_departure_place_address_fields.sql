ALTER TABLE departure_place
    ADD COLUMN road_address VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN place_name   VARCHAR(100);

ALTER TABLE departure_place
    ALTER COLUMN road_address DROP DEFAULT;
