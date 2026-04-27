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

COMMENT ON TABLE terms IS '약관 (유형별 버전 관리)';
COMMENT ON COLUMN terms.id IS '약관 고유 ID';
COMMENT ON COLUMN terms.type IS '약관 유형 (TERMS_OF_SERVICE/PRIVACY_POLICY/MARKETING)';
COMMENT ON COLUMN terms.version IS '약관 버전 (예: 1.0)';
COMMENT ON COLUMN terms.title IS '약관 제목';
COMMENT ON COLUMN terms.content IS '약관 본문';
COMMENT ON COLUMN terms.is_required IS '필수 동의 여부 (true면 미동의 시 가입 불가)';
COMMENT ON COLUMN terms.effective_from IS '약관 시행일';
COMMENT ON COLUMN terms.created_at IS '등록 시각';

COMMENT ON TABLE terms_agreement IS '약관 동의 이력 (DELETE 금지 - 법적 증적)';
COMMENT ON COLUMN terms_agreement.id IS '동의 이력 고유 ID';
COMMENT ON COLUMN terms_agreement.member_id IS '동의한 회원 ID';
COMMENT ON COLUMN terms_agreement.terms_id IS '동의한 약관 ID';
COMMENT ON COLUMN terms_agreement.agreed_at IS '동의 시각';
