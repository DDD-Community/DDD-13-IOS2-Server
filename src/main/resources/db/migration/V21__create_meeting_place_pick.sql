-- meeting_place_pick: FC-9 장소 담기
CREATE TABLE meeting_place_pick (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id  BIGINT      NOT NULL REFERENCES meeting(id),
    member_id   BIGINT      NOT NULL REFERENCES member(id),
    place_id    BIGINT      NOT NULL REFERENCES place(id),
    picked_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_meeting_member_place UNIQUE (meeting_id, member_id, place_id)
);

COMMENT ON TABLE meeting_place_pick IS 'FC-9 장소 담기 — 모임원별 담은 장소 목록';
COMMENT ON COLUMN meeting_place_pick.picked_at IS '담은 시각';

-- meeting 테이블에 담기 마감일시 컬럼 추가
ALTER TABLE meeting ADD COLUMN pick_deadline TIMESTAMPTZ;

COMMENT ON COLUMN meeting.pick_deadline IS 'FC-9 담기 마감일시 (장소 추천 완료 시점 +3일 23:59:59)';
