ALTER TABLE meeting ADD COLUMN category_labels TEXT[];
ALTER TABLE meeting ADD COLUMN vibes TEXT[];
ALTER TABLE meeting ADD COLUMN reservable BOOLEAN;
ALTER TABLE meeting ADD COLUMN parking BOOLEAN;

COMMENT ON COLUMN meeting.category_labels IS 'FC-8 추천용 음식 카테고리 선호 (고정 11종 중 선택, 선택 입력)';
COMMENT ON COLUMN meeting.vibes IS 'FC-8 추천용 분위기 태그 선호 (place.vibe 표준목록, 선택 입력)';
COMMENT ON COLUMN meeting.reservable IS 'FC-8 추천 HARD 필터 — 예약가능한 곳만. 생성 시 입력, NULL=조건 없음';
COMMENT ON COLUMN meeting.parking IS 'FC-8 추천 HARD 필터 — 주차가능한 곳만. 생성 시 입력, NULL=조건 없음';
COMMENT ON COLUMN meeting.location_status IS '장소 선정 상태 (BEFORE/RECOMMENDED/VOTING/CONFIRMED)';
