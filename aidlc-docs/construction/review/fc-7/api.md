# API 명세 — FC-7 모임 상세 + 날짜 투표

## 공통

- Base URL: `/api/v1/meetings`
- 인증: JWT Bearer Token 필수
- 에러 응답 형식: `{ "code": "MEETING_001", "message": "..." }`

---

## 1. 모임 상세 조회

```
GET /api/v1/meetings/{meetingId}
```

**Response 200**
```json
{
  "meetingId": 1,
  "name": "팀 회식",
  "themeTagCode": "DINING",
  "themeTagDisplay": "회식",
  "locationStatus": "BEFORE",
  "dateVoteStatus": "IN_PROGRESS",
  "confirmedDate": null,
  "members": [
    {
      "memberId": 10,
      "nickname": "홍길동",
      "profileImageUrl": "https://...",
      "isHost": true,
      "isMe": true,
      "departurePlaces": [
        {
          "id": 1,
          "label": "집",
          "address": "서울 강남구 삼성동 159",
          "roadAddress": "서울 강남구 영동대로 513",
          "placeName": "카카오프렌즈 코엑스점",
          "latitude": 37.123,
          "longitude": 127.456,
          "isDefault": true
        }
      ]
    }
  ]
}
```

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |

---

## 2. 날짜 투표 — 방식 A: 호스트 단독 선택

```
POST /api/v1/meetings/{meetingId}/date-vote/host-pick
```

**Request**
```json
{ "date": "2026-06-15" }
```

**Response**: 204 No Content

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |
| 호스트 아님 | GROUP_004 | 403 |
| 이미 투표 시작됨 | MEETING_002 | 400 |
| 날짜가 오늘 이전 | MEETING_006 | 400 |

---

## 3. 날짜 투표 — 방식 B: 투표 시작

```
POST /api/v1/meetings/{meetingId}/date-vote
```

**Request**
```json
{
  "candidateDates": ["2026-06-10", "2026-06-14", "2026-06-17"],
  "durationDays": 3
}
```

**Response**: 204 No Content

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |
| 호스트 아님 | GROUP_004 | 403 |
| 이미 투표 시작됨 | MEETING_002 | 400 |
| 후보 날짜 개수 오류 (1~10개 아님) | MEETING_007 | 400 |
| 후보 날짜가 오늘 이전이거나 중복 | MEETING_006 | 400 |
| durationDays가 1/3/7 아님 | MEETING_008 | 400 |

---

## 4. 투표 참여

```
POST /api/v1/meetings/{meetingId}/date-vote/submit
```

**Request**
```json
{ "optionIds": [1, 3] }
```

**Response**: 204 No Content

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |
| 투표 진행 중 아님 | MEETING_003 | 400 |
| 마감일 지남 | MEETING_004 | 400 |
| 유효하지 않은 optionId | MEETING_005 | 400 |

**비고**: 재투표 허용 (기존 기록 삭제 후 재저장). 전원 투표 완료 시 즉시 자동 처리.

---

## 5. 투표 현황 조회

```
GET /api/v1/meetings/{meetingId}/date-vote
```

**Response 200**
```json
{
  "dateVoteStatus": "IN_PROGRESS",
  "sessionStatus": "ACTIVE",
  "deadline": "2026-06-13",
  "options": [
    {
      "optionId": 1,
      "candidateDate": "2026-06-10",
      "voteCount": 3,
      "isMyVote": true,
      "voters": [
        {
          "memberId": 10,
          "nickname": "홍길동",
          "profileImageUrl": "https://..."
        }
      ]
    }
  ]
}
```

**비고**: 투표 세션 없을 경우 options = []. 정렬: voteCount DESC → sort_order ASC.

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |

---

## 6. 호스트 수동 날짜 확정

```
PATCH /api/v1/meetings/{meetingId}/date-vote/confirm
```

**Request**
```json
{ "optionId": 2 }
```

**Response**: 204 No Content

**에러**
| 상황 | 코드 | HTTP |
|---|---|---|
| 모임 없음 | MEETING_001 | 404 |
| 그룹 구성원 아님 | GROUP_003 | 403 |
| 호스트 아님 | GROUP_004 | 403 |
| 투표 진행 중 아님 | MEETING_003 | 400 |
| 유효하지 않은 optionId | MEETING_005 | 400 |
