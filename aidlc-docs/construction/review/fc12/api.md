# API 명세 — FC-12 투표 진행

## POST /api/v1/meetings/{meetingId}/place-vote/submit
### 요청
```json
{ "placeIds": [12, 45] }
```
- 다중제한(후보 50% 내림, 최소1) 검증. 익명 저장
### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 400 | PLACE_VOTE_NOT_IN_PROGRESS | VOTING 아님/마감됨 |
| 400 | PLACE_VOTE_LIMIT_EXCEEDED | 50% 초과 |

## GET /api/v1/meetings/{meetingId}/place-vote
- 후보별 득표수(익명 집계), 내 투표, 모임원 투표완료 현황(호스트)
- 후보별 이동부담: 참여자별 소요시간·환승수, 최장 이동자 표시
- 각 후보의 장소 정보는 `place`(PlaceSummary, FC-8 참조)로 내려감. 중복 `placeId` 필드는 제거됨
```json
{
  "deadline": "2026-06-25", "sessionStatus": "OPEN",
  "totalParticipants": 4, "votedCount": 2,
  "candidates": [
    {
      "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
      "voteCount": 3, "isMyVote": true,
      "travelBurdens": [ { "memberId": 1, "seconds": 1800, "transfers": 1, "isLongest": false } ]
    }
  ]
}
```
