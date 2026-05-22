# Business Rules — FC-6 모임 리스트

## 조회 규칙

- 자신이 속한 **모든 그룹**(GroupStatus 무관, ACTIVE + CLOSED 포함)의 최신 모임을 반환한다.
- 그룹당 **1개의 모임 카드**만 표시한다 — 가장 최근에 생성된 meeting.
- 속한 그룹이 없으면 빈 배열(`[]`)을 반환한다.
- `locationAddress`는 MVP2 미구현으로 항상 `null`을 반환한다.

## 정렬 규칙

- **1순위**: `listStatus` 우선순위 (IN_PROGRESS → CONFIRMED → CLOSED)
- **2순위 (동일 status)**: `meeting.created_at` 내림차순 (최신 모임이 위)
- 구성원 목록은 `group_member.joined_at` 오름차순 (합류 오래된 순)

## 인가 규칙

- **JWT 인증 필수** — 미인증 시 401 반환
- 본인이 속한 그룹만 조회 가능 — 다른 사용자의 그룹은 응답에 포함되지 않는다

## 제약사항

| 항목 | 내용 |
|---|---|
| 그룹 상태 필터 | 없음 — CLOSED 그룹 포함 |
| 모임 선택 기준 | 그룹의 가장 최근 생성 meeting (meeting.id 기준 최댓값) |
| 장소 주소 | 항상 null (MVP2 대응) |

## 예외 케이스

| 상황 | 처리 |
|---|---|
| 속한 그룹 없음 | 빈 배열 반환, 에러 아님 |
| 그룹은 있으나 meeting이 없음 | 발생 불가 (FC-4에서 그룹 생성 시 항상 meeting 동시 생성) |
| 모임 날짜가 지났으나 confirmedDate null | CLOSED 아님 — IN_PROGRESS로 계산 |
