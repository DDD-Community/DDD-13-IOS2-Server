# NFR Design Patterns — Unit 4 (FC-7)

## 1. 인가 패턴 (Security Baseline)

기존 코드(GroupService, MeetingListService)에 인가 체크가 없어 FC-7에서 처음 패턴 정립.

### 서비스 레이어 Guard 패턴

모든 인가가 필요한 서비스 메서드 최상단에 guard 블록 배치.

```java
// 멤버 확인 (403)
GroupMember caller = groupMemberRepository
    .findByGroupIdAndMemberId(groupId, memberId)
    .orElseThrow(() -> new BusinessException(NOT_GROUP_MEMBER));

// 호스트 확인 (403, 호스트 전용 API만 추가)
if (caller.getRole() != GroupMemberRole.HOST) {
    throw new BusinessException(NOT_HOST);
}
```

### ErrorCode 추가 필요

```java
NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "해당 그룹의 구성원이 아닙니다"),
NOT_HOST(HttpStatus.FORBIDDEN, "호스트만 수행할 수 있는 작업입니다"),
```

### GroupMemberRepository 메서드 추가

```java
Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);
```

---

## 2. 트랜잭션 패턴

| 메서드 유형 | 설정 |
|---|---|
| 조회 메서드 | `@Transactional(readOnly = true)` |
| 쓰기 메서드 | `@Transactional` |
| 스케줄러 오케스트레이션 | 트랜잭션 없음 (루프만) |
| 스케줄러 건별 처리 | 별도 `@Transactional` 메서드로 분리 |

---

## 3. 스케줄러 패턴

건별 실패가 전체 배치 중단하지 않도록 루프 + try-catch 패턴.

```java
@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
public void processScheduled() {
    // 투표 자동 확정
    List<DateVoteSession> expiredSessions = dateVoteSessionRepository.findActiveByDeadline(LocalDate.now().minusDays(1));
    for (DateVoteSession session : expiredSessions) {
        try {
            voteSchedulerService.processExpiredSession(session.getId());
        } catch (Exception e) {
            log.error("투표 자동 확정 실패 sessionId={}", session.getId(), e);
        }
    }

    // 모임 자동 종료 (기존 FC-6 로직과 통합)
    List<Meeting> expiredMeetings = meetingRepository.findExpiredMeetings(LocalDate.now());
    for (Meeting meeting : expiredMeetings) {
        try {
            meetingSchedulerService.closeMeeting(meeting.getId());
        } catch (Exception e) {
            log.error("모임 자동 종료 실패 meetingId={}", meeting.getId(), e);
        }
    }
}

// 별도 @Transactional 메서드 — 건별 트랜잭션 독립
@Transactional
public void processExpiredSession(Long sessionId) { ... }
```

---

## 4. 상태 검증 패턴

상태 전이 조건을 도메인 메서드로 캡슐화.

```java
// Meeting 도메인에 추가
public void startVote() {
    if (this.dateVoteStatus != DateVoteStatus.BEFORE) {
        throw new BusinessException(VOTE_ALREADY_STARTED);
    }
    this.dateVoteStatus = DateVoteStatus.IN_PROGRESS;
}

public void confirmDate(LocalDate date) {
    this.confirmedDate = date;
    this.dateVoteStatus = DateVoteStatus.COMPLETED;
}

public void resetVote() {
    this.dateVoteStatus = DateVoteStatus.BEFORE;
}
```

---

## 5. 출발지 잠금 패턴

```java
// DeparturePlaceService.update() 등에서 호출 시 체크
Meeting meeting = meetingRepository.findCurrentByGroupId(groupId);
if (meeting.getDateVoteStatus() == DateVoteStatus.COMPLETED) {
    throw new BusinessException(DEPARTURE_PLACE_LOCKED);
}
```
