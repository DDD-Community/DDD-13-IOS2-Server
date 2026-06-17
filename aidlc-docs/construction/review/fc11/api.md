# API 명세 — FC-11 투표 생성 + 마감일

## POST /api/v1/meetings/{meetingId}/place-vote
**설명**: 투표 세션 생성(마감일 설정) + VOTING 전환 + 이동부담 스냅샷
**권한**: 호스트(또는 자동전환 내부 호출)

### 요청
```json
{ "durationDays": 3 }
```
| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| durationDays | Number | ✅ | 1 / 3 / 7 중 하나 |

### 에러
| 상태 | 코드 | 조건 |
|---|---|---|
| 400 | INVALID_DURATION_DAYS | 1·3·7 외 |
| 400 | PLACE_VOTE_DEADLINE_INVALID | 마감일 < 시작 or ≥ 약속일 |
| 400 | LOCATION_NOT_RECOMMENDED | 담기 단계 아님 |
