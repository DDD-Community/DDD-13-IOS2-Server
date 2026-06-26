# FC-7 비즈니스 규칙

## 모임 상세 조회

### 인가 규칙
- 해당 그룹의 멤버만 조회 가능 (비멤버 403)

### 표시 규칙
- 구성원 목록: joined_at 오름차순 (합류 오래된 순)
- 출발지: member의 departure_place 전체 (없으면 빈 배열)

---

## 날짜 투표 — 방식 A (host-pick)

### 인가 규칙
- HOST만 호출 가능

### 생성/변경 규칙
- dateVoteStatus = BEFORE 상태에서만 실행 가능
- 모임 일시는 **날짜+시간**(LocalDateTime). 선택 일시는 현재보다 미래여야 함
- 호출 즉시 confirmed_date(날짜+시간) 설정 + dateVoteStatus = COMPLETED
- FCM 알림 없음 (PRD 명시)

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| 일시 | 현재보다 미래 (날짜+시간) | 400 |
| 상태 | BEFORE만 허용 | 400 |

---

## 날짜 투표 — 방식 B (vote)

### 인가 규칙
- HOST만 시작 가능
- 모든 멤버(HOST 포함) 투표 참여 가능

### 생성/변경 규칙
- dateVoteStatus = BEFORE 상태에서만 시작 가능
- deadline = 투표 시작일 + durationDays (자정 기준)
- 후보 일시(날짜+시간): 1~10개, 현재보다 미래, 중복 불가
- 재투표: 마감일 전까지 허용 (기존 기록 덮어씀)
- optionIds 빈 배열 제출 시 기존 투표 전체 취소

### 인가 규칙 — 투표 참여
- 해당 그룹 멤버만 가능
- 마감일 이후 제출 불가 (400)

### 자동 확정 규칙 (스케줄러)
- 투표자 있을 때: 최다 득표 일시 자동 확정. 동률 시 sort_order 낮은 것 우선
- 투표자 없을 때: dateVoteStatus = BEFORE 리셋, 호스트 FCM 발송

### 호스트 수동 확정 규칙
- dateVoteStatus = IN_PROGRESS 상태에서만 가능
- 선택한 optionId는 해당 세션 소속이어야 함

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| 후보 일시 수 | 1~10개 | 400 |
| durationDays | 1 / 3 / 7 중 하나 | 400 |
| 투표 참여 시점 | 마감일 이전만 가능 | 400 |
| optionId | 해당 세션 소속만 허용 | 400 |

---

## 출발지 / 참석여부 잠금

| 항목 | 잠금 시점 | MVP |
|---|---|---|
| 출발지 수정 | dateVoteStatus = COMPLETED 이후 | MVP1 |
| 참석여부 수정 (미팅별, meeting_participant) | locationStatus = COMPLETED 이후 | MVP2 (MVP1 잠금 없음) — 그룹 단위 참석여부 API는 제거됨 |

---

## 예외 케이스

| 상황 | 처리 |
|---|---|
| IN_PROGRESS인데 투표 시작 요청 | 400 |
| COMPLETED인데 투표 시작 요청 | 400 |
| 마감 후 투표 제출 | 400 |
| 동률 자동 확정 | sort_order 낮은 후보(먼저 입력) 선택 |
| 투표자 없이 마감 | BEFORE 리셋, 호스트 알림 |
| 세션 없는데 현황 조회 | 빈 options 배열 반환 (BEFORE 상태) |
