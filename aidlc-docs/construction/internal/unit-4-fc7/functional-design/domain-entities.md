# Domain Entities — FC-7 모임 상세 + 날짜 투표

## 신규 도메인 모델

### DateVoteMethod (enum)
```
HOST_PICK  — 호스트 단독 선택 (즉시 확정)
VOTE       — 구성원 투표
```

### SessionStatus (enum)
```
ACTIVE     — 투표 진행 중
EXPIRED    — 마감됨 (투표자 없음, 호스트 재선택 대기)
CONFIRMED  — 날짜 확정됨
```

---

### DateVoteSession (date_vote_session 테이블)

투표 한 라운드의 세션. meeting당 최대 1개 존재.

```
id           : Long
meetingId    : Long
method       : DateVoteMethod
deadline     : LocalDate      // VOTE 방식만 유효 (HOST_PICK 시 null)
durationDays : Integer        // 1 / 3 / 7 (HOST_PICK 시 null)
status       : SessionStatus
createdAt    : LocalDateTime
```

팩토리 메서드:
- `DateVoteSession.ofHostPick(meetingId)` → method=HOST_PICK, status=CONFIRMED, deadline=null
- `DateVoteSession.ofVote(meetingId, durationDays)` → method=VOTE, deadline=오늘+durationDays, status=ACTIVE

비즈니스 메서드:
- `confirm()` → status=CONFIRMED
- `expire()` → status=EXPIRED

---

### DateVoteOption (date_vote_option 테이블)

투표 후보 날짜. session당 최대 3개.

```
id            : Long
sessionId     : Long
candidateDate : LocalDate
sortOrder     : int   // 호스트가 선택한 순서 (0부터)
```

---

### DateVoteRecord (date_vote_record 테이블)

구성원 투표 기록.

```
id       : Long
optionId : Long
memberId : Long
votedAt  : LocalDateTime
```

DB UNIQUE 제약: `(option_id, member_id)` — 같은 후보에 중복 투표 불가

---

## 신규 Repository 인터페이스 (meeting/domain/)

### DateVoteSessionRepository
```
findByMeetingId(Long meetingId): Optional<DateVoteSession>
findActiveByDeadline(LocalDate deadline): List<DateVoteSession>  // 스케줄러용
save(DateVoteSession session): DateVoteSession
```

### DateVoteOptionRepository
```
findBySessionId(Long sessionId): List<DateVoteOption>
saveAll(List<DateVoteOption> options): List<DateVoteOption>
```

### DateVoteRecordRepository
```
findByOptionIdIn(List<Long> optionIds): List<DateVoteRecord>
existsByOptionIdAndMemberId(Long optionId, Long memberId): boolean
saveAll(List<DateVoteRecord> records): List<DateVoteRecord>
```

---

## 신규 애플리케이션 서비스

### MeetingDetailService (meeting/application/)
```
getMeetingDetail(meetingId: Long, currentMemberId: Long): MeetingDetailResponse
```

의존성: MeetingRepository, GroupMemberRepository, MemberRepository, ThemeTagRepository, DeparturePlaceRepository

### DateVoteService (meeting/application/)
```
startHostPick(meetingId: Long, memberId: Long, date: LocalDate)
startVote(meetingId: Long, memberId: Long, request: StartVoteRequest)
submitVote(meetingId: Long, memberId: Long, optionIds: List<Long>)
getVoteStatus(meetingId: Long, memberId: Long): VoteStatusResponse
confirmDate(meetingId: Long, memberId: Long, optionId: Long)
processExpiredVotes()  // @Scheduled 호출
```

의존성: MeetingRepository, GroupMemberRepository, DateVoteSessionRepository, DateVoteOptionRepository, DateVoteRecordRepository, MemberRepository, FCMService

---

## 신규 DTO (meeting/presentation/dto/)

### MeetingDetailResponse
```
groupId        : Long
meetingId      : Long
name           : String
themeTagCode   : String
themeTagDisplay: String
dateVoteStatus : String    // BEFORE / IN_PROGRESS / COMPLETED
locationStatus : String    // BEFORE / IN_PROGRESS / COMPLETED
confirmedDate  : LocalDate // nullable
members        : List<MemberDetailInfo>
myInfo         : MemberDetailInfo
```

### MemberDetailInfo
```
memberId         : Long
nickname         : String
profileImageUrl  : String          // nullable
attendanceStatus : String          // JOIN / LATE / ABSENT
departurePlaces  : List<String>    // 출발지 주소 목록 (N개, 없으면 빈 배열)
```

### StartVoteRequest (방식 B)
```
candidateDates : List<LocalDate>  // 최대 3개, 오늘 이후 날짜
durationDays   : int              // 1 / 3 / 7
```

### VoteStatusResponse
```
sessionId     : Long
method        : String       // HOST_PICK / VOTE
deadline      : LocalDate    // nullable
confirmedDate : LocalDate    // nullable
options       : List<VoteOptionInfo>
```

### VoteOptionInfo
```
optionId      : Long
candidateDate : LocalDate
voteCount     : int
voters        : List<VoterInfo>
isMyVote      : boolean
```

### VoterInfo
```
memberId       : Long
nickname       : String
profileImageUrl: String  // nullable
```

---

## 신규 컨트롤러 엔드포인트

```
GET   /api/v1/meetings/{meetingId}
      → MeetingDetailService.getMeetingDetail
      → 200 OK, MeetingDetailResponse

POST  /api/v1/meetings/{meetingId}/date-vote/host-pick
      body: { "date": "2026-06-15" }
      → DateVoteService.startHostPick
      → 200 OK

POST  /api/v1/meetings/{meetingId}/date-vote
      body: StartVoteRequest
      → DateVoteService.startVote
      → 200 OK

POST  /api/v1/meetings/{meetingId}/date-vote/submit
      body: { "optionIds": [1, 3] }
      → DateVoteService.submitVote
      → 200 OK

GET   /api/v1/meetings/{meetingId}/date-vote
      → DateVoteService.getVoteStatus
      → 200 OK, VoteStatusResponse

PATCH /api/v1/meetings/{meetingId}/date-vote/confirm
      body: { "optionId": 2 }
      → DateVoteService.confirmDate
      → 200 OK
```
