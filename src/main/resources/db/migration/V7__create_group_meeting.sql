-- theme_tag: 테마 태그 (DB 관리, 코드 재배포 없이 추가/변경 가능)
CREATE TABLE theme_tag (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code         VARCHAR(30)  NOT NULL,
    display_name VARCHAR(50)  NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_theme_tag_code UNIQUE (code)
);

COMMENT ON TABLE theme_tag IS '테마 태그 (코드 재배포 없이 추가/변경 가능)';
COMMENT ON COLUMN theme_tag.code IS '태그 코드 (예: BUSINESS, SOCIAL)';
COMMENT ON COLUMN theme_tag.display_name IS '화면 표시 이름 (예: 비즈니스, 친목)';
COMMENT ON COLUMN theme_tag.sort_order IS '정렬 순서';
COMMENT ON COLUMN theme_tag.is_active IS '활성 여부 (false면 신규 선택 불가, 기존 데이터는 유지)';

-- 기본 테마 태그 시드 데이터
INSERT INTO theme_tag (code, display_name, sort_order) VALUES
    ('BUSINESS',    '비즈니스',    1),
    ('SOCIAL',      '친목',        2),
    ('FAMILY',      '가족모임',    3),
    ('DINING',      '회식',        4),
    ('CASUAL_MEAL', '간단한 식사', 5),
    ('STUDY',       '스터디',      6),
    ('BIRTHDAY',    '생일파티',    7),
    ('WEDDING',     '청첩장 모임', 8);

-- group_info: 그룹
CREATE TABLE group_info (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(30)  NOT NULL,
    theme_tag_code VARCHAR(30)  NOT NULL REFERENCES theme_tag(code),
    status         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE group_info IS '그룹 (group은 예약어라 group_info 사용)';
COMMENT ON COLUMN group_info.name IS '그룹명 (모임명과 동일한 값, 최대 30자)';
COMMENT ON COLUMN group_info.theme_tag_code IS '테마 태그 코드 (theme_tag.code 참조)';
COMMENT ON COLUMN group_info.status IS '그룹 상태 (ACTIVE/CLOSED)';

-- meeting: 모임
CREATE TABLE meeting (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_id         BIGINT       NOT NULL REFERENCES group_info(id),
    name             VARCHAR(30)  NOT NULL,
    theme_tag_code   VARCHAR(30)  NOT NULL REFERENCES theme_tag(code),
    location_status  VARCHAR(15)  NOT NULL DEFAULT 'BEFORE',
    date_vote_status VARCHAR(15)  NOT NULL DEFAULT 'BEFORE',
    confirmed_date   DATE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE meeting IS '모임 (그룹 내 반복 생성)';
COMMENT ON COLUMN meeting.group_id IS '소속 그룹 ID';
COMMENT ON COLUMN meeting.name IS '모임명 (그룹 생성 시 그룹명과 동일한 값)';
COMMENT ON COLUMN meeting.theme_tag_code IS '테마 태그 코드';
COMMENT ON COLUMN meeting.location_status IS '장소 선정 상태 (BEFORE/IN_PROGRESS/COMPLETED)';
COMMENT ON COLUMN meeting.date_vote_status IS '날짜 투표 상태 (BEFORE/IN_PROGRESS/COMPLETED)';
COMMENT ON COLUMN meeting.confirmed_date IS '확정된 모임 날짜 (미확정 시 NULL)';

-- group_member: 그룹 구성원
CREATE TABLE group_member (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_id          BIGINT       NOT NULL REFERENCES group_info(id),
    member_id         BIGINT       NOT NULL REFERENCES member(id),
    role              VARCHAR(10)  NOT NULL,
    attendance_status VARCHAR(10)  NOT NULL DEFAULT 'JOIN',
    joined_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_group_member UNIQUE (group_id, member_id)
);

COMMENT ON TABLE group_member IS '그룹 구성원';
COMMENT ON COLUMN group_member.group_id IS '소속 그룹 ID';
COMMENT ON COLUMN group_member.member_id IS '구성원 회원 ID';
COMMENT ON COLUMN group_member.role IS '역할 (HOST/MEMBER)';
COMMENT ON COLUMN group_member.attendance_status IS '참석여부 (JOIN/LATE/ABSENT), 기본값 JOIN';
COMMENT ON COLUMN group_member.joined_at IS '그룹 합류 시각';
