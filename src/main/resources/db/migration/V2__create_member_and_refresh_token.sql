-- member 테이블: 소셜 로그인 정보 + 프로필 통합
CREATE TABLE member (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    social_provider VARCHAR(20)  NOT NULL,
    social_user_id  VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    nickname        VARCHAR(20),
    profile_image_url VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_registered   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT uk_member_social UNIQUE (social_provider, social_user_id)
);

COMMENT ON TABLE member IS '회원 (소셜 로그인 정보 + 프로필)';
COMMENT ON COLUMN member.id IS '회원 고유 ID';
COMMENT ON COLUMN member.social_provider IS '소셜 공급자 (KAKAO, NAVER, APPLE)';
COMMENT ON COLUMN member.social_user_id IS '소셜 공급자가 부여한 사용자 고유 ID';
COMMENT ON COLUMN member.email IS '이메일 (애플은 비공개 가능, nullable)';
COMMENT ON COLUMN member.nickname IS '닉네임 (null이면 회원가입 미완료)';
COMMENT ON COLUMN member.profile_image_url IS '프로필 이미지 URL (MVP에서는 미사용)';
COMMENT ON COLUMN member.status IS '회원 상태 (ACTIVE/SUSPENDED/WITHDRAWN)';
COMMENT ON COLUMN member.is_registered IS '회원가입 완료 여부 (true = 약관 + 닉네임 + 출발지 등록 완료)';
COMMENT ON COLUMN member.created_at IS '최초 소셜 로그인 시각';
COMMENT ON COLUMN member.updated_at IS '마지막 정보 수정 시각';
COMMENT ON COLUMN member.deleted_at IS '탈퇴 시각 (null이면 활동 중)';

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

COMMENT ON TABLE refresh_token IS 'JWT Refresh Token (해시만 저장)';
COMMENT ON COLUMN refresh_token.id IS '토큰 고유 ID';
COMMENT ON COLUMN refresh_token.member_id IS '토큰 소유 회원 ID';
COMMENT ON COLUMN refresh_token.token_hash IS '토큰 SHA-256 해시 (원문 저장 X)';
COMMENT ON COLUMN refresh_token.expires_at IS '토큰 만료 시각';
COMMENT ON COLUMN refresh_token.revoked_at IS '토큰 폐기 시각 (null이면 유효)';
COMMENT ON COLUMN refresh_token.created_at IS '토큰 발급 시각';
