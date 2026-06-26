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
- 후보별 **득표수(익명 집계) + 내 투표 여부**만 제공
- 🔄 (2026-06-25) **이동부담 제거** — PRD 12-3(투표 현황 = 득표수·참여인원) 범위 정렬. 이동부담은 친구들 거리보기 API에서만 제공
- ⭐ 🔄 `memberStatuses[]`(구성원별 완료여부)는 **모든 호출자(호스트·구성원)** 에게 제공 (2026-06-24 변경: 전원 공개)
- 각 후보 장소 정보는 `place`(PlaceSummary)로 내려감
```json
{
  "deadline": "2026-06-25", "sessionStatus": "OPEN",
  "totalParticipants": 4, "votedCount": 2,
  "memberStatuses": [ { "memberId": 1, "name": "홍길동", "completed": true } ],
  "candidates": [
    {
      "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
      "voteCount": 3, "isMyVote": true
    }
  ]
}
```
> `memberStatuses`는 **모든 응답에 채워짐**(호스트/구성원 무관). 익명성: 완료여부만 노출, 투표 대상은 비공개.

## GET /api/v1/meetings/{meetingId}/place-vote/{placeId}/travel-burden ⭐🔄 (친구들 거리보기)
- 단일 장소에 대한 **모임 활성 참여자 전원**(요청자 포함)의 소요시간·환승·경로 목록. 상세 화면 "친구들 거리보기" 버튼용.
- 데이터 = `meeting_travel_burden` 스냅샷(투표 시작 시 저장분). 신규 계산 없음.
- 권한: 모임 구성원.
- ⭐ (2026-06-25) 멤버별 항목에 `path` 추가 — 출발역→도착역 경로(역 좌표 순서 리스트), 지도 표시용. 스냅샷 저장분, 도달 불가 시 `[]`.
- ⭐ (2026-06-25 보강) 멤버 기준 = 활성 참여자 전원. 스냅샷 없는 멤버도 포함(`seconds`/`transfers`=null, `path`=[]).
- ⭐ 멤버별 추가 필드: `departureName`(출발지 이름, nullable), `isMe`(요청자 본인 여부). `isLongest`는 소요시간 보유 멤버 중 최대만 true.
- ⭐ (2026-06-26) `path[]` 항목에 `order`(출발 0→도착 순서 인덱스) + `isDeparture`(첫 역) + `isArrival`(마지막=도착역) 추가. JSON 배열 순서는 보장되지만 클라 polyline 안전장치로 `order` 명시. `stationName`은 **도착역(isArrival)만 채우고 나머지는 null** — 모든 멤버 도착역은 같은 장소 최근접역이라 역명 조회 1건이면 충분(전 구간 조회 안 함). 도착역 역명은 `subway_station` 조회, 미존재 시 null.
```json
{
  "place": { "placeId": 12, "name": "○○식당", "categoryLabel": "RESTAURANT", "address": "서울 ...", "latitude": 37.5, "longitude": 127.0 },
  "burdens": [
    {
      "memberId": 1, "name": "홍길동", "departureName": "집", "isMe": true,
      "seconds": 1800, "transfers": 1, "isLongest": false,
      "path": [
        { "stationId": 201, "stationName": null,   "latitude": 37.49, "longitude": 127.02, "order": 0, "isDeparture": true,  "isArrival": false },
        { "stationId": 202, "stationName": null,   "latitude": 37.50, "longitude": 127.01, "order": 1, "isDeparture": false, "isArrival": false },
        { "stationId": 245, "stationName": "선릉", "latitude": 37.50, "longitude": 127.00, "order": 2, "isDeparture": false, "isArrival": true  }
      ]
    },
    { "memberId": 2, "name": "김철수", "departureName": "회사", "isMe": false,
      "seconds": 3000, "transfers": 2, "isLongest": true, "path": [] },
    { "memberId": 3, "name": "이영희", "departureName": null, "isMe": false,
      "seconds": null, "transfers": null, "isLongest": false, "path": [] }
  ]
}
```

## GET /api/v1/meetings/{meetingId}/place-vote/participants ⭐ (2026-06-25 신규 — 장소투표 참여 팀원 조회)
- 현재 **VOTING 상태** 모임의 **활성 참여자(ABSENT 제외) 전원** 목록.
- 멤버별: `memberId`, `name`(nickname), `profileImageUrl`(원본 object key — 클라이언트 resolve), `departureName`(저장된 출발지 메타, nullable), `isMe`(본인여부), `voted`(현재 세션에 1표+ 제출 여부).
- 권한: 모임 그룹원. 상태: VOTING 아니면 `PLACE_VOTE_NOT_IN_PROGRESS`.
- `departureName`은 `meeting_participant`에 저장된 메타(placeName→label) 기반. 좌표 역매칭 없음.
```json
{
  "participants": [
    { "memberId": 1, "name": "홍길동", "profileImageUrl": "profile/1/abc.jpg", "departureName": "집",  "isMe": true,  "voted": true },
    { "memberId": 2, "name": "김철수", "profileImageUrl": "profile/2/def.jpg", "departureName": "회사", "isMe": false, "voted": false },
    { "memberId": 3, "name": "이영희", "profileImageUrl": null,                "departureName": null,   "isMe": false, "voted": false }
  ]
}
```
- 에러: 404 `MEETING_NOT_FOUND` / 403 `NOT_GROUP_MEMBER` / 409 `PLACE_VOTE_NOT_IN_PROGRESS`
