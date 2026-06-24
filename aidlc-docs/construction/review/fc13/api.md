# API 명세 — FC-13 자동 확정

> 2026-06-24 갱신. ⭐ 수동확정 엔드포인트 + 후보 rank 추가.

## (시스템) 자동 확정
- 전원 투표완료 후처리 또는 투표마감 스케줄러가 수행.

## POST /api/v1/meetings/{meetingId}/place-confirm ⭐ (수동 확정)
- 호스트 전용, 상태 VOTING 에서만. 현재 순위 비교자로 1위 확정 → CONFIRMED 전환.
### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 403 | NOT_GROUP_HOST | 호스트 아님 |
| 400 | PLACE_VOTE_NOT_IN_PROGRESS | VOTING 아님 |

## GET /api/v1/meetings/{meetingId}/place-result
- 확정 장소 `place`(PlaceSummary) + `confirmedAt` + 후보별 `candidates[]`(득표/이동부담 요약 + ⭐ `rank`)
- ⭐ candidates는 순위 비교자로 정렬, 1~3위 `rank` 부여(후보<3이면 후보수만큼)
- 미확정 시 PLACE_NOT_CONFIRMED(400)
```json
{
  "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
  "confirmedAt": "2026-06-21T12:00:00",
  "candidates": [
    { "rank": 1, "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
      "voteCount": 3, "totalSeconds": 7200, "totalTransfers": 4 }
  ]
}
```
