# API 명세 — FC-9 담기

## GET /api/v1/meetings/{meetingId}/places
조회. 쿼리: `stationId`(역탭), `category`, `reservable`, `parking`
- 응답: 카드 목록(placeId, name, categoryLabel, roadAddress, vibes≤3, cardDistance, pickCount, pickedByMe)

## POST /api/v1/meetings/{meetingId}/places/{placeId}/pick
- 담기. 멱등(이미 담음이면 유지)

## DELETE /api/v1/meetings/{meetingId}/places/{placeId}/pick
- 담기 취소

## GET /api/v1/meetings/{meetingId}/places/pick-status
- 모임원별 담기완료 여부(프로필 체크용), 내 담은 목록

## POST /api/v1/meetings/{meetingId}/place-vote (호스트 '투표 생성하기')
- 후보≥1 시 즉시 VOTING 전환 (FC-11로 연결, 마감일 포함)

## 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 400 | PLACE_PICK_CLOSED | 담기 마감 후 담기 시도 |
| 400 | LOCATION_NOT_RECOMMENDED | RECOMMENDED 아닌 상태에서 담기 |
