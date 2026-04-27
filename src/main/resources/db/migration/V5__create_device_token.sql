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

COMMENT ON TABLE device_token IS 'iOS 디바이스 토큰 (푸시 알림용, 발송은 후순위)';
COMMENT ON COLUMN device_token.id IS '토큰 고유 ID';
COMMENT ON COLUMN device_token.member_id IS '소유 회원 ID';
COMMENT ON COLUMN device_token.token IS 'APNs 디바이스 토큰';
COMMENT ON COLUMN device_token.platform IS '플랫폼 (IOS)';
COMMENT ON COLUMN device_token.app_version IS '앱 버전';
COMMENT ON COLUMN device_token.created_at IS '등록 시각';
COMMENT ON COLUMN device_token.updated_at IS '갱신 시각';
