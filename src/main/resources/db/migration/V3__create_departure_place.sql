CREATE TABLE departure_place (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member(id),
    label       VARCHAR(10)  NOT NULL,
    address     VARCHAR(255) NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    geo_point   GEOGRAPHY(POINT, 4326) NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_departure_place_member ON departure_place(member_id);
