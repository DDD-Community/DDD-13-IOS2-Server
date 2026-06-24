# API 명세 — FC-13 자동 확정

## (시스템) 자동 확정
- 전원 투표완료 후처리 또는 투표마감 스케줄러가 수행. 직접 호출 엔드포인트 없음

## GET /api/v1/meetings/{meetingId}/place-result
- 확정 장소 `place`(PlaceSummary) + `confirmedAt` + 후보별 `candidates[]`(득표/이동부담 요약)
- 확정 장소·후보 모두 동일한 `place`(PlaceSummary, FC-8 참조) 형태로 통일. 과거 top-level `placeId/placeName/address` 평탄 필드는 제거됨
- 미확정 시 PLACE_NOT_CONFIRMED(400) 또는 진행상태 반환
```json
{
  "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
  "confirmedAt": "2026-06-21T12:00:00",
  "candidates": [
    { "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
      "voteCount": 3, "totalSeconds": 7200, "totalTransfers": 4 }
  ]
}
```
