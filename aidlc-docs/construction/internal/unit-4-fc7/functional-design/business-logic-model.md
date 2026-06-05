# Business Logic Model — FC-7 모임 상세 + 날짜 투표

## 날짜 투표 상태머신 (DateVoteStatus on Meeting)

```
BEFORE
  ├─→ [host-pick API] → COMPLETED  (confirmed_date 즉시 설정, 세션 생성 없음)
  └─→ [vote API]      → IN_PROGRESS (date_vote_session 생성, 후보 날짜 등록)

IN_PROGRESS
  ├─→ [submit API]         → IN_PROGRESS 유지 (투표 기록만 추가)
  ├─→ [confirm API: 호스트] → COMPLETED  (호스트 수동 확정)
  └─→ [스케줄러: 마감 자정]
        ├─ 투표자 있음 → COMPLETED (1위 자동 확정, session.status=CONFIRMED)
        └─ 투표자 없음 → BEFORE 리셋 (session.status=EXPIRED, FCM to 호스트)

COMPLETED
  → 불변. confirmed_date 확정. 출발지 잠금 시작.
```

---

## 플로우별 상세 로직

### 방식 A: 호스트 단독 선택 (host-pick)

```
1. 인가 확인: 호출자가 해당 그룹의 HOST인지 검증
2. 상태 확인: meeting.dateVoteStatus == BEFORE 인지 검증
3. 날짜 검증: 선택 날짜 > 오늘
4. meeting.confirmedDate = 선택 날짜
5. meeting.dateVoteStatus = COMPLETED
6. meeting.updatedAt = 현재시각
7. (알림 없음 — PRD 명시)
```

### 방식 B: 구성원 투표 시작 (vote)

```
1. 인가 확인: 호출자가 HOST인지 검증
2. 상태 확인: meeting.dateVoteStatus == BEFORE 인지 검증
3. 입력 검증:
   - candidateDates: 1~3개, 각 날짜 > 오늘, 중복 없음
   - durationDays: 1 / 3 / 7 중 하나
4. date_vote_session 생성:
   - method = VOTE
   - deadline = 오늘 + durationDays (LocalDate)
   - status = ACTIVE
5. date_vote_option 생성 (후보 날짜 수만큼, sort_order = 입력 순서)
6. meeting.dateVoteStatus = IN_PROGRESS
7. FCM: 그룹 구성원 전체에 "투표 시작" 알림 발송
```

### 투표 참여 (submit)

```
1. 인가 확인: 호출자가 그룹 멤버인지 검증
2. 상태 확인: meeting.dateVoteStatus == IN_PROGRESS
3. 마감일 확인: 오늘 <= session.deadline
4. optionId 목록 검증: 해당 세션에 속한 option들인지 확인
5. 기존 투표 기록 삭제 (재투표 허용 — 마감일 전까지)
6. date_vote_record 저장 (optionId별로)
```

### 투표 현황 조회 (getVoteStatus)

```
1. 인가 확인: 그룹 멤버인지 검증
2. date_vote_session 조회 (meetingId 기준)
3. date_vote_option 목록 조회
4. date_vote_record 조회 → option별 투표자 집계
5. 정렬: voteCount DESC, sort_order ASC (동률 시 sort_order 낮은 것 우선)
6. isMyVote: 각 option에 내 record 있으면 true
```

### 호스트 수동 확정 (confirmDate)

```
1. 인가 확인: 호출자가 HOST인지 검증
2. 상태 확인: meeting.dateVoteStatus == IN_PROGRESS
3. optionId 검증: 해당 세션에 속한 option인지 확인
4. meeting.confirmedDate = option.candidateDate
5. meeting.dateVoteStatus = COMPLETED
6. session.status = CONFIRMED
7. FCM: 구성원 전체에 "날짜 확정" 알림 발송
```

---

## 스케줄러 로직 (@Scheduled, 매일 자정)

실행 순서 (한 배치에서 처리):

```
[Step 1] 마감 투표 처리
  - date_vote_session WHERE status=ACTIVE AND deadline < 오늘 조회
  - 각 세션별:
    a) 투표 기록 있음 (date_vote_record 존재):
       - 최다 득표 option 선택 (동률 시 sort_order 낮은 것)
       - meeting.confirmedDate = option.candidateDate
       - meeting.dateVoteStatus = COMPLETED
       - session.status = CONFIRMED
       - FCM: 구성원 전체 "날짜 확정" 알림
    b) 투표 기록 없음:
       - meeting.dateVoteStatus = BEFORE (리셋)
       - session.status = EXPIRED
       - FCM: 호스트에게 "투표자 없음" 알림

[Step 2] 모임 자동 종료
  - meeting WHERE confirmedDate < 오늘 AND dateVoteStatus = COMPLETED 조회
  - meeting.locationStatus 상관없이 만남일 지나면 CLOSED 처리
  (기존 FC-6 설계와 동일 배치에서 실행)
```

---

## 모임 상세 조회 로직

```
1. 인가 확인: 호출자가 그룹 멤버인지 검증
2. meeting 조회 (meetingId)
3. group 조회 → groupId 확인
4. themeTag 조회 → display_name
5. groupMember 목록 조회 (joined_at ASC 정렬)
6. member 정보 조회 (nickname, profileImageUrl)
7. departure_place 조회 (member_id별 전체 목록)
8. 응답 조립:
   - members: 전원 (호출자 포함)
   - myInfo: 호출자 정보 (members 목록과 동일 구조)
```

---

## 출발지 잠금 규칙

| 상태 | 출발지 수정 가능 여부 |
|---|---|
| `dateVoteStatus = BEFORE / IN_PROGRESS` | 가능 |
| `dateVoteStatus = COMPLETED` | 불가 (403) |

MVP1: 참석여부는 잠금 없음 (CLOSED 될 때까지 자유 변경)
MVP2: `locationStatus = COMPLETED` 이후 참석여부 잠금 추가 예정
