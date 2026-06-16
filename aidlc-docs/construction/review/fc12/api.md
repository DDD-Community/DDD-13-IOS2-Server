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
