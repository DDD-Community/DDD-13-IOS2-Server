# Logical Components — Unit 4 (FC-7)

## 컴포넌트 구성

```
[MeetingController]
    │
    ├── MeetingDetailService        ← 모임 상세 조회
    └── DateVoteService             ← 날짜 투표 (host-pick / vote / submit / confirm)

[MeetingScheduler]  ← @Scheduled
    ├── VoteSchedulerService        ← 투표 자동 확정 건별 처리
    └── MeetingSchedulerService     ← 모임 자동 종료 건별 처리
```

## 신규 컴포넌트

| 컴포넌트 | 레이어 | 역할 |
|---|---|---|
| `MeetingDetailService` | application | 모임 상세 조회 오케스트레이션 |
| `DateVoteService` | application | 날짜 투표 전체 유스케이스 |
| `MeetingScheduler` | infrastructure | @Scheduled 진입점, 루프 + try-catch |
| `VoteSchedulerService` | application | 건별 투표 확정 처리 (@Transactional) |
| `MeetingSchedulerService` | application | 건별 모임 종료 처리 (@Transactional) |
| `DateVoteSessionRepository` | domain | 투표 세션 도메인 리포지토리 |
| `DateVoteOptionRepository` | domain | 후보 날짜 도메인 리포지토리 |
| `DateVoteRecordRepository` | domain | 투표 기록 도메인 리포지토리 |

## 외부 의존성

| 의존성 | 상태 |
|---|---|
| FCM | MVP1 제외 (no-op) |
| SSE | 미사용 |
| 외부 큐/캐시 | 없음 |

## 패키지 구조

```
meeting/
  application/
    MeetingDetailService.java
    DateVoteService.java
    VoteSchedulerService.java
    MeetingSchedulerService.java
  domain/
    DateVoteSession.java
    DateVoteMethod.java       (enum)
    SessionStatus.java        (enum)
    DateVoteOption.java
    DateVoteRecord.java
    DateVoteSessionRepository.java
    DateVoteOptionRepository.java
    DateVoteRecordRepository.java
  infrastructure/
    persistence/
      DateVoteSessionJpaEntity.java
      DateVoteSessionJpaRepository.java
      DateVoteSessionRepositoryImpl.java
      DateVoteOptionJpaEntity.java
      DateVoteOptionJpaRepository.java
      DateVoteOptionRepositoryImpl.java
      DateVoteRecordJpaEntity.java
      DateVoteRecordJpaRepository.java
      DateVoteRecordRepositoryImpl.java
    scheduler/
      MeetingScheduler.java
  presentation/
    dto/
      MeetingDetailResponse.java
      StartVoteRequest.java
      VoteStatusResponse.java
      HostPickRequest.java
      ConfirmDateRequest.java
      SubmitVoteRequest.java

global/
  error/
    ErrorCode.java            ← NOT_GROUP_MEMBER, NOT_HOST, VOTE_ALREADY_STARTED 등 추가
```
