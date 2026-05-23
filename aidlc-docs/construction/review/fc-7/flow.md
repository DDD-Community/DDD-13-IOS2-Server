# FC-7 로직 흐름

## 1. 모임 상세 조회

```
GET /api/v1/meetings/{meetingId}

[Controller] → [MeetingDetailService]
  1. JWT에서 memberId 추출
  2. meeting 조회 (meetingId)
  3. groupMember 조회 → 호출자가 이 그룹 멤버인지 확인 (없으면 403)
  4. 같은 그룹의 groupMember 전체 조회 (joined_at ASC 정렬)
  5. member 정보 조회 (nickname, profileImageUrl)
  6. departure_place 조회 (member별 전체 목록)
  7. themeTag 조회 → displayName
  8. 응답 조립 → MeetingDetailResponse 반환
```

---

## 2. 방식 A — 호스트 단독 날짜 선택 (host-pick)

```
POST /api/v1/meetings/{meetingId}/date-vote/host-pick
body: { "date": "2026-06-15" }

[Controller] → [DateVoteService.startHostPick]
  1. JWT에서 memberId 추출
  2. meeting 조회
  3. groupMember 조회 → HOST인지 확인 (아니면 403)
  4. meeting.dateVoteStatus == BEFORE 확인 (아니면 400)
  5. date > 오늘 확인 (아니면 400)
  6. meeting.confirmedDate = date
  7. meeting.dateVoteStatus = COMPLETED  ← 즉시 확정
  8. meeting 저장
  ※ 알림 없음 (PRD 명시)
```

---

## 3. 방식 B — 투표 시작

```
POST /api/v1/meetings/{meetingId}/date-vote
body: { "candidateDates": ["2026-06-10", "2026-06-14", "2026-06-17"], "durationDays": 3 }

[Controller] → [DateVoteService.startVote]
  1. JWT에서 memberId 추출
  2. meeting 조회
  3. groupMember 조회 → HOST 확인 (아니면 403)
  4. meeting.dateVoteStatus == BEFORE 확인 (아니면 400)
  5. candidateDates 검증 (1~3개, 오늘 이후, 중복 없음)
  6. durationDays 검증 (1/3/7 중 하나)
  7. date_vote_session 생성
       method=VOTE, deadline=오늘+durationDays, status=ACTIVE
  8. date_vote_option 생성 (후보 날짜 수만큼, sort_order=입력순서)
  9. meeting.dateVoteStatus = IN_PROGRESS
  10. meeting, session, options 저장
  ※ FCM 알림 → MVP1 no-op
```

---

## 4. 투표 참여

```
POST /api/v1/meetings/{meetingId}/date-vote/submit
body: { "optionIds": [1, 3] }

[Controller] → [DateVoteService.submitVote]
  1. JWT에서 memberId 추출
  2. meeting 조회
  3. groupMember 조회 → 멤버 확인 (아니면 403)
  4. meeting.dateVoteStatus == IN_PROGRESS 확인 (아니면 400)
  5. session 조회 → deadline >= 오늘 확인 (마감됐으면 400)
  6. optionIds가 이 session 소속인지 확인 (아니면 400)
  7. 기존 이 멤버의 투표 기록 삭제 (재투표 허용)
  8. date_vote_record 저장 (optionId별로)
     ※ DB UNIQUE (option_id, member_id) — 중복 방지

  [전원 투표 완료 체크 — 마감일 전 조기 종료]
  9. 그룹 전체 멤버 수(N) 조회
  10. 이번 session에서 투표한 distinct 멤버 수(M) 조회
  11. N == M 이면 → 스케줄러와 동일한 로직 즉시 실행:
        명확한 1위 → COMPLETED 처리 (meeting.confirmedDate 설정, session.status = CONFIRMED)
        동률       → BEFORE 리셋, session.status = EXPIRED
                     // TODO: FCM — 호스트에게 "동률, 직접 선택 요청" 알림
```

---

## 5. 투표 현황 조회

```
GET /api/v1/meetings/{meetingId}/date-vote

[Controller] → [DateVoteService.getVoteStatus]
  1. JWT에서 memberId 추출
  2. meeting 조회
  3. groupMember 조회 → 멤버 확인 (아니면 403)
  4. session 조회 (없으면 빈 options 반환)
  5. option 목록 조회
  6. record 목록 조회 (option_id IN 목록)
  7. option별 투표자 집계
       voteCount = 이 option에 투표한 record 수
       voters = 투표자 member 정보
       isMyVote = 내 memberId로 기록 있으면 true
  8. 정렬: voteCount DESC → sort_order ASC
  9. VoteStatusResponse 반환
```

---

## 6. 호스트 수동 확정

```
PATCH /api/v1/meetings/{meetingId}/date-vote/confirm
body: { "optionId": 2 }

[Controller] → [DateVoteService.confirmDate]
  1. JWT에서 memberId 추출
  2. meeting 조회
  3. groupMember 조회 → HOST 확인 (아니면 403)
  4. meeting.dateVoteStatus == IN_PROGRESS 확인 (아니면 400)
  5. option 조회 → 이 session 소속인지 확인 (아니면 400)
  6. meeting.confirmedDate = option.candidateDate
  7. meeting.dateVoteStatus = COMPLETED
  8. session.status = CONFIRMED
  9. meeting, session 저장
  ※ FCM 알림 → MVP1 no-op
```

---

## 7. 스케줄러 — 매일 자정 자동 처리

### 실행 환경

```
cron   : "0 0 0 * * *"  (매일 00:00:00 KST)
zone   : "Asia/Seoul"
구현체  : MeetingScheduler (@Scheduled 진입점)
트랜잭션: 건별 독립 — MeetingScheduler는 @Transactional 없음
          실제 처리는 VoteSchedulerService / MeetingSchedulerService의
          @Transactional 메서드에 위임 → 한 건 실패해도 다른 건에 영향 없음
```

### 처리 순서

```
Step 1  투표 마감 대상 조회
        SELECT * FROM date_vote_session
        WHERE status = 'ACTIVE'
          AND deadline < 오늘 날짜   ← 마감일 지난 것만
        결과: List<DateVoteSession>

Step 2  각 세션별 처리 (VoteSchedulerService.processExpiredSession)
        try {
          session의 option 목록 조회
          각 option별 투표 기록 수(COUNT) 집계

          [Case A] 명확한 1위 존재 (최다 득표 option이 1개)
            → meeting.confirmedDate = 해당 option.candidateDate
            → meeting.dateVoteStatus = COMPLETED
            → session.status = CONFIRMED
            → meeting, session 저장
            → // TODO: FCM — 구성원 전체 "날짜 확정" 알림 (FCM 유닛 완료 후 구현)

          [Case B] 투표자 없음 (total count == 0)
            → session.status = EXPIRED
            → meeting.dateVoteStatus = BEFORE  (호스트가 재선택 가능하도록 리셋)
            → session, meeting 저장
            → // TODO: FCM — 호스트에게 "투표자 없음, 직접 날짜 선택 요청" 알림

          [Case C] 동률 (최다 득표 option이 2개 이상)
            → session.status = EXPIRED
            → meeting.dateVoteStatus = BEFORE  (자동 확정 불가, 호스트가 결정)
            → session, meeting 저장
            → // TODO: FCM — 호스트에게 "동률 발생, 직접 날짜 선택 요청" 알림
        } catch (Exception e) {
          log.error("투표 자동 확정 실패 sessionId={}", session.getId(), e)
          // 다음 세션으로 계속 진행
        }

        ※ 모임 종료(CLOSED) 처리는 별도 배치 불필요.
           meeting.computeListStatus()가 confirmedDate < today 이면 CLOSED를 동적으로 반환하므로
           스케줄러에서 별도 처리하지 않는다.
```

### 미래 알림 추가 포인트 (FCM 유닛 완료 후)

```
현재 no-op 처리된 위치에 FCM 호출 추가:
  1. 투표 마감 전날 자정   → 아직 투표 안 한 구성원에게 리마인더
     (별도 스케줄러 메서드 또는 Step 2 실행 전 D-1 세션 조회 추가)
  2. 투표 자동 확정 후     → 구성원 전체에 "날짜 확정" 알림
  3. 투표자 없음 마감 후   → 호스트에게 "직접 날짜 선택" 요청 알림
```

---

## 상태 전이 요약

```
DateVoteStatus:

BEFORE      : 투표 시작 전 초기 상태. 모임 생성 시 기본값.
              실패(투표자 없음 / 동률) 후 리셋될 때도 이 상태로 돌아옴.
IN_PROGRESS : 투표 진행 중. 구성원이 투표 가능한 기간.
COMPLETED   : 날짜 확정됨. confirmed_date 세팅 완료.

BEFORE ──[host-pick]──────────────────────────────→ COMPLETED
BEFORE ──[vote 시작]──────────────────────────────→ IN_PROGRESS
IN_PROGRESS ──[호스트 confirm]────────────────────→ COMPLETED
IN_PROGRESS ──[스케줄러: 명확한 1위]───────────────→ COMPLETED
IN_PROGRESS ──[스케줄러: 투표자 없음]──────────────→ BEFORE (리셋)
IN_PROGRESS ──[스케줄러: 동률]─────────────────────→ BEFORE (리셋, 호스트 결정 필요)
```
