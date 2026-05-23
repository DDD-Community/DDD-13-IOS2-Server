# Business Rules — FC-7 모임 상세 + 날짜 투표

## 모임 상세 조회

### 인가 규칙
- JWT 인증 필수 (401)
- 해당 그룹의 멤버만 조회 가능 (403)

### 표시 규칙
- 구성원 목록: `group_member.joined_at` 오름차순 (합류 오래된 순)
- 출발지: 해당 member의 `departure_place` 전체 목록 (N개, 없으면 빈 배열)
- myInfo: 호출자 본인 정보 (members 목록과 동일 구조)

---

## 방식 A — 호스트 단독 선택 (host-pick)

### 인가 규칙
- 해당 그룹의 HOST만 호출 가능 (403)

### 실행 조건
- `meeting.dateVoteStatus == BEFORE` (위반 시 400)

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| 선택 날짜 | 오늘보다 미래 | 400 |
| 알림 | 없음 | — |

### 결과
- `meeting.confirmedDate` 설정, `dateVoteStatus = COMPLETED`

---

## 방식 B — 투표 시작

### 인가 규칙
- 해당 그룹의 HOST만 호출 가능 (403)

### 실행 조건
- `meeting.dateVoteStatus == BEFORE` (위반 시 400)
- BEFORE로 리셋된 경우(투표자 없음 만료 후)도 재시작 가능

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| 후보 날짜 수 | 1~3개 | 400 |
| 후보 날짜 값 | 오늘보다 미래, 중복 없음 | 400 |
| durationDays | 1, 3, 7 중 하나 | 400 |

### 결과
- `date_vote_session` 생성 (deadline = 오늘 + durationDays)
- `meeting.dateVoteStatus = IN_PROGRESS`
- FCM: 구성원 전체 "투표 시작" 알림

---

## 투표 참여 (submit)

### 인가 규칙
- 해당 그룹의 멤버만 호출 가능 (403)
- 호스트도 투표 가능

### 실행 조건
- `meeting.dateVoteStatus == IN_PROGRESS` (위반 시 400)
- `오늘 <= session.deadline` (마감일 이후 불가, 400)

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| optionId 유효성 | 해당 세션에 속한 option만 허용 | 400 |
| 재투표 | 허용 (기존 기록 덮어씀) | — |
| 빈 목록 | optionIds 빈 배열 전달 시 기존 투표 전체 취소 | — |

---

## 투표 현황 조회

### 인가 규칙
- 해당 그룹의 멤버만 조회 가능 (403)

### 정렬
- `voteCount DESC`, 동률 시 `sort_order ASC`

---

## 호스트 수동 확정 (confirm)

### 인가 규칙
- 해당 그룹의 HOST만 호출 가능 (403)

### 실행 조건
- `meeting.dateVoteStatus == IN_PROGRESS` (위반 시 400)

### 제약사항
| 항목 | 제약 | 위반 시 |
|---|---|---|
| optionId | 해당 세션에 속한 option만 허용 | 400 |

### 결과
- `meeting.confirmedDate` 설정, `dateVoteStatus = COMPLETED`, `session.status = CONFIRMED`
- FCM: 구성원 전체 "날짜 확정" 알림

---

## 스케줄러 자동 처리

### 실행 조건
- 매일 자정 실행
- 대상: `session.status = ACTIVE AND session.deadline < 오늘`

### 처리 규칙
| 케이스 | 처리 |
|---|---|
| 투표자 있음 | 최다 득표 자동 확정. 동률 시 sort_order 낮은 것 우선 |
| 투표자 없음 | session.status=EXPIRED, meeting.dateVoteStatus=BEFORE 리셋, 호스트 FCM 알림 |

---

## 출발지 / 참석여부 수정 잠금

| 항목 | 잠금 조건 | MVP |
|---|---|---|
| 출발지 수정 | `dateVoteStatus = COMPLETED` 이후 | MVP1 적용 |
| 참석여부 수정 | `locationStatus = COMPLETED` 이후 | MVP2 적용 (MVP1 잠금 없음) |

---

## 예외 케이스

| 상황 | 처리 |
|---|---|
| 이미 투표 진행 중인데 투표 시작 요청 | 400 (BEFORE 상태만 시작 가능) |
| 확정된 날짜에 다시 투표 시작 요청 | 400 (COMPLETED 상태) |
| 동률 발생 (스케줄러 자동 확정) | sort_order 낮은 것 (먼저 입력한 후보) 우선 |
| 투표자 없이 마감 | BEFORE 리셋, 호스트 FCM. 호스트가 다시 A/B 선택 가능 |
| meetingId에 세션 없는데 현황 조회 | dateVoteStatus=BEFORE면 빈 options 배열 반환 |
