# Unit 4 (FC-7) Code Generation Plan

## 컨텍스트

- **브랜치**: feature/fc7-meeting-detail
- **신규 테이블**: date_vote_session, date_vote_option, date_vote_record
- **수정 파일**: Meeting.java, MeetingRepository, MeetingController, ErrorCode, DeparturePlaceRepository
- **기존 재사용**: GroupMemberRepository.findByGroupIdAndMemberId ✓, DeparturePlaceRepository.findAllByMemberId ✓, MemberRepository.findAllById ✓, GroupMemberRepository.findByGroupIdIn ✓

## API 목록

| Method | URL | 설명 |
|---|---|---|
| GET | /api/v1/meetings/{meetingId} | 모임 상세 |
| POST | /api/v1/meetings/{meetingId}/date-vote/host-pick | 방식 A: 호스트 단독 선택 즉시 확정 |
| POST | /api/v1/meetings/{meetingId}/date-vote | 방식 B: 투표 시작 |
| POST | /api/v1/meetings/{meetingId}/date-vote/submit | 투표 참여 |
| GET | /api/v1/meetings/{meetingId}/date-vote | 현날짜 투표 현황 조회 |
| PATCH | /api/v1/meetings/{meetingId}/date-vote/confirm | 호스트 수동 확정 (optionId 선택) |

---

## Group A — DB & 공통

- [ ] **Step 1**: `V8__create_date_vote_tables.sql` 생성
  - date_vote_session, date_vote_option, date_vote_record 테이블
  - UNIQUE (option_id, member_id) 제약

- [ ] **Step 2**: `global/error/ErrorCode.java` 수정
  - MEETING_NOT_FOUND, VOTE_ALREADY_STARTED, VOTE_CLOSED, VOTE_OPTION_NOT_FOUND 추가
  - (NOT_GROUP_MEMBER, NOT_GROUP_HOST 이미 존재)

---

## Group B — Domain 모델

- [ ] **Step 3**: `meeting/domain/DateVoteMethod.java` 생성 (enum: HOST_PICK, VOTE)

- [ ] **Step 4**: `meeting/domain/SessionStatus.java` 생성 (enum: ACTIVE, EXPIRED, CONFIRMED)

- [ ] **Step 5**: `meeting/domain/DateVoteSession.java` 생성
  - 팩토리: ofHostPick(meetingId), ofVote(meetingId, durationDays)
  - 메서드: confirm(), expire()

- [ ] **Step 6**: `meeting/domain/DateVoteOption.java` 생성

- [ ] **Step 7**: `meeting/domain/DateVoteRecord.java` 생성

- [ ] **Step 8**: `meeting/domain/Meeting.java` 수정
  - startVote(), confirmDate(LocalDate), resetVote() 메서드 추가

---

## Group C — Repository 인터페이스

- [ ] **Step 9**: `meeting/domain/DateVoteSessionRepository.java` 생성
  - findByMeetingId, findActiveByDeadlineBefore, save

- [ ] **Step 10**: `meeting/domain/DateVoteOptionRepository.java` 생성
  - findBySessionId, saveAll

- [ ] **Step 11**: `meeting/domain/DateVoteRecordRepository.java` 생성
  - findByOptionIdIn, deleteByOptionIdInAndMemberId, saveAll

- [ ] **Step 12**: `meeting/domain/MeetingRepository.java` 수정
  - findExpiredMeetings(LocalDate today) 추가

- [ ] **Step 13**: `member/domain/departure/DeparturePlaceRepository.java` 수정
  - findAllByMemberIdIn(List<Long> memberIds) 추가

---

## Group D — Infrastructure (JPA Entity)

- [ ] **Step 14**: `meeting/infrastructure/persistence/DateVoteSessionJpaEntity.java` 생성

- [ ] **Step 15**: `meeting/infrastructure/persistence/DateVoteOptionJpaEntity.java` 생성

- [ ] **Step 16**: `meeting/infrastructure/persistence/DateVoteRecordJpaEntity.java` 생성

---

## Group E — Infrastructure (JPA Repository)

- [ ] **Step 17**: `meeting/infrastructure/persistence/DateVoteSessionJpaRepository.java` 생성

- [ ] **Step 18**: `meeting/infrastructure/persistence/DateVoteOptionJpaRepository.java` 생성

- [ ] **Step 19**: `meeting/infrastructure/persistence/DateVoteRecordJpaRepository.java` 생성

---

## Group F — Infrastructure (Repository Impl)

- [ ] **Step 20**: `meeting/infrastructure/persistence/DateVoteSessionRepositoryImpl.java` 생성

- [ ] **Step 21**: `meeting/infrastructure/persistence/DateVoteOptionRepositoryImpl.java` 생성

- [ ] **Step 22**: `meeting/infrastructure/persistence/DateVoteRecordRepositoryImpl.java` 생성

- [ ] **Step 23**: `meeting/infrastructure/persistence/MeetingRepositoryImpl.java` 수정
  - findExpiredMeetings 구현 추가

- [ ] **Step 24**: `meeting/infrastructure/persistence/MeetingJpaRepository.java` 수정
  - findExpiredMeetings 쿼리 추가

- [ ] **Step 25**: `member/infrastructure/persistence/departure/DeparturePlaceRepositoryImpl.java` 수정
  - findAllByMemberIdIn 구현 추가

- [ ] **Step 26**: `member/infrastructure/persistence/departure/DeparturePlaceJpaRepository.java` 수정
  - findAllByMemberIdIn 추가

---

## Group G — DTOs

- [ ] **Step 27**: `meeting/presentation/dto/MeetingDetailResponse.java` 생성 (record)

- [ ] **Step 28**: `meeting/presentation/dto/HostPickRequest.java` 생성 (record)

- [ ] **Step 29**: `meeting/presentation/dto/StartVoteRequest.java` 생성 (record)

- [ ] **Step 30**: `meeting/presentation/dto/SubmitVoteRequest.java` 생성 (record)

- [ ] **Step 31**: `meeting/presentation/dto/ConfirmDateRequest.java` 생성 (record)

- [ ] **Step 32**: `meeting/presentation/dto/VoteStatusResponse.java` 생성 (record)

---

## Group H — Application Services

- [ ] **Step 33**: `meeting/application/MeetingDetailService.java` 생성

- [ ] **Step 34**: `meeting/application/DateVoteService.java` 생성

---

## Group I — Scheduler

- [ ] **Step 35**: `meeting/application/VoteSchedulerService.java` 생성

- [ ] **Step 36**: `meeting/infrastructure/scheduler/MeetingScheduler.java` 생성

---

## Group J — Controller & Review

- [ ] **Step 38**: `meeting/presentation/MeetingController.java` 수정
  - GET /{id}, POST /host-pick, POST /date-vote, POST /submit, GET /date-vote, PATCH /confirm 추가

- [ ] **Step 39**: `aidlc-docs/construction/review/fc-7/api.md` 생성

---

**총 39 Steps**
