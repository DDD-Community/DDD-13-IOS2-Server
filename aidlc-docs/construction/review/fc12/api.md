# API 명세 — FC-12 투표 진행

> 2026-06-24 갱신 (mvp3-1 갭). 후보=담긴 장소, placeId 검증, 호스트 완료현황 추가.

## POST /api/v1/meetings/{meetingId}/place-vote/submit
### 요청
```json
{ "placeIds": [12, 45] }
```
- ⭐ 다중제한 = **담긴 후보** 50% 내림(최소1) 검증. 익명 저장
- ⭐ placeId 전부 현재 후보집합(담김+백필)에 속해야 함
### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 400 | PLACE_VOTE_NOT_IN_PROGRESS | VOTING 아님/마감됨 |
| 400 | PLACE_VOTE_LIMIT_EXCEEDED | 담긴 후보 50% 초과 |
| 400 | PLACE_VOTE_INVALID_CANDIDATE ⭐ | 후보 아닌 placeId 제출 |

## GET /api/v1/meetings/{meetingId}/place-vote
- ⭐ 후보 = 담긴 장소(백필 포함). 정렬: 투표 전 가나다순 / 후 득표순
- 후보별 득표수(익명 집계), 내 투표, 후보별 이동부담(참여자별 소요시간·환승수, 최장 이동자)
- ⭐ 호스트 호출 시 `memberStatuses[]`(구성원별 완료여부) 포함. 구성원 호출 시 null/미포함
- 각 후보 장소 정보는 `place`(PlaceSummary)로 내려감
```json
{
  "deadline": "2026-06-25", "sessionStatus": "OPEN",
  "totalParticipants": 4, "votedCount": 2,
  "memberStatuses": [ { "memberId": 1, "name": "홍길동", "completed": true } ],
  "candidates": [
    {
      "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
      "voteCount": 3, "isMyVote": true,
      "travelBurdens": [ { "memberId": 1, "seconds": 1800, "transfers": 1, "isLongest": false } ]
    }
  ]
}
```
> `memberStatuses`는 호스트 응답에만 채워짐(익명성: 완료여부만, 투표 대상 비공개).
