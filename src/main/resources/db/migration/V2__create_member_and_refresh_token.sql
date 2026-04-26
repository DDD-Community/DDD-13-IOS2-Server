-- member 테이블: 소셜 로그인 정보 + 프로필 통합
CREATE TABLE member (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    social_provider VARCHAR(20)  NOT NULL,
    social_user_id  VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    nickname        VARCHAR(20),
    profile_image_url VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uk_member_social UNIQUE (social_provider, social_user_id)
);

-- refresh_token 테이블: 토큰 해시만 저장 (원문 저장 X)
CREATE TABLE refresh_token (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES member(id),
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_member ON refresh_token(member_id);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);
