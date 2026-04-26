CREATE TABLE terms (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type           VARCHAR(30)  NOT NULL,
    version        VARCHAR(20)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    content        TEXT         NOT NULL,
    is_required    BOOLEAN      NOT NULL,
    effective_from TIMESTAMPTZ  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_terms_type_version UNIQUE (type, version)
);

CREATE TABLE terms_agreement (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id  BIGINT      NOT NULL REFERENCES member(id),
    terms_id   BIGINT      NOT NULL REFERENCES terms(id),
    agreed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_agreement UNIQUE (member_id, terms_id)
);
-- DELETE 금지: 법적 증적. 애플리케이션 레벨에서 강제.
