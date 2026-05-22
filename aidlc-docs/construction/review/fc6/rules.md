# 비즈니스 규칙 — FC-6 모임 리스트

## 조회 규칙

- 로그인 사용자가 속한 **모든 그룹의 최신 모임**을 카드 목록으로 반환한다.
- 그룹 상태(ACTIVE/CLOSED)와 무관하게 전체 포함한다.
- 그룹당 카드 1장 — 해당 그룹의 가장 최근 생성 meeting 1개를 기준으로 표시.
- 속한 그룹이 없으면 빈 배열을 반환한다 (에러 아님).


## 정렬 규칙

| 순위 | 기준 | 방향 |
|---|---|---|
| 1순위 | listStatus (IN_PROGRESS → CONFIRMED → CLOSED) | 진행 중이 최상단 |
| 2순위 | meeting.created_at | 내림차순 (최신 모임이 위) |
| 구성원 | group_member.joined_at | 오름차순 (먼저 합류한 순) |

## 인가 규칙

- **JWT 인증 필수** — 미인증 요청은 401 반환
- 본인이 group_member로 등록된 그룹만 조회됨 (추가 필터 불필요, 조회 쿼리 구조 자체가 보장)

## 상태 계산 규칙 (MeetingListStatus)

| 조건 | 상태 |
|---|---|
| confirmedDate != null AND confirmedDate < 오늘 | CLOSED |
| locationStatus = IN_PROGRESS OR dateVoteStatus = IN_PROGRESS | IN_PROGRESS |
| locationStatus = COMPLETED AND dateVoteStatus = COMPLETED | CONFIRMED |
| 그 외 (둘 다 BEFORE 등 초기 상태) | IN_PROGRESS |

## 제약사항

| 항목 | 제약 | 위반 시 |
|---|---|---|
| JWT | 필수 | 401 |
| locationAddress | 항상 null (MVP2 미구현) | — |

## 예외 케이스

| 상황 | 처리 |
|---|---|
| confirmedDate null이고 날짜 지남 | CLOSED 아님, IN_PROGRESS 유지 |
| 그룹은 있으나 meeting 없음 | 발생 불가 (FC-4에서 항상 동시 생성) |
| 탈퇴한 회원의 프로필 | nickname/profileImageUrl null 허용 (null-safe 처리) |
