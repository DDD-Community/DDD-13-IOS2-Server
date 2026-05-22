# Business Rules — FC-7-1 내 정보 수정

## 참석여부 수정 규칙

| 규칙 | 내용 | 위반 시 |
|---|---|---|
| 본인만 수정 | JWT memberId가 group_member.member_id와 일치해야 함 | 403 NOT_GROUP_MEMBER |
| 그룹 멤버 확인 | 해당 groupId의 구성원이어야 함 | 403 NOT_GROUP_MEMBER |
| 유효한 status | JOIN / LATE / ABSENT 중 하나 | 400 INVALID_INPUT |

## 출발지 추가 규칙

| 규칙 | 내용 | 위반 시 |
|---|---|---|
| 최대 3개 | 회원당 출발지 최대 3개 | 400 DEPARTURE_PLACE_LIMIT_EXCEEDED |
| 첫 등록 방어 | 출발지가 0개일 때는 isDefault 강제 true | — (서버 자동 처리) |
| isDefault 충돌 방지 | isDefault=true 요청 시 기존 default 자동 해제 | — (서버 자동 처리) |

## 출발지 수정 규칙

| 규칙 | 내용 | 위반 시 |
|---|---|---|
| 본인만 수정 | JWT memberId 소유 출발지만 수정 가능 | 404 DEPARTURE_PLACE_NOT_FOUND |
| 변경 가능 필드 | label, address, latitude, longitude | — |
| isDefault 변경 불가 | PUT 요청에서 isDefault 무시 | — (필드 자체 미노출) |

## 에러 코드 정리

| 코드 | HTTP | 설명 |
|---|---|---|
| AUTH_001 | 401 | JWT 없음 또는 만료 |
| GROUP_003 (NOT_GROUP_MEMBER) | 403 | 해당 그룹 구성원 아님 |
| MEMBER_003 (DEPARTURE_PLACE_LIMIT_EXCEEDED) | 400 | 출발지 3개 초과 |
| MEMBER_005 (DEPARTURE_PLACE_NOT_FOUND) | 404 | 출발지 없음 또는 타인 소유 |
