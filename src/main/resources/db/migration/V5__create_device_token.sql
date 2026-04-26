CREATE TABLE device_token (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member(id),
    token       VARCHAR(500) NOT NULL,
    platform    VARCHAR(10)  NOT NULL DEFAULT 'IOS',
    app_version VARCHAR(20),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_device_token UNIQUE (member_id, token)
);
