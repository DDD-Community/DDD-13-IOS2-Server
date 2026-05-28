CREATE TABLE group_invite (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_id   BIGINT      NOT NULL REFERENCES group_info(id),
    code       VARCHAR(36) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_group_invite_code UNIQUE (code)
);

CREATE INDEX idx_group_invite_code ON group_invite(code);

COMMENT ON TABLE group_invite IS '그룹 초대 코드';
COMMENT ON COLUMN group_invite.code IS 'UUID 기반 초대 코드 (36자)';
COMMENT ON COLUMN group_invite.expires_at IS '초대 코드 만료 시각 (발급 후 48시간)';
