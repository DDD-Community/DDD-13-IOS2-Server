-- FC-12/13 보완: 투표 후보 백필 지원
-- meeting_place_pick에 출처(source) 추가 + 시스템 백필 행을 위해 member_id nullable 전환

ALTER TABLE meeting_place_pick
    ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'USER';

ALTER TABLE meeting_place_pick
    ALTER COLUMN member_id DROP NOT NULL;

COMMENT ON COLUMN meeting_place_pick.source IS '담기 출처 — USER(구성원 직접) / SYSTEM(후보 3개 미만 시 추천 자동 백필)';
COMMENT ON COLUMN meeting_place_pick.member_id IS '담은 구성원 — SYSTEM 백필 행은 NULL';
