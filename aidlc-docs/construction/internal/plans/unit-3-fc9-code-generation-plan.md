# U3 담기(FC-9) — Code Generation Plan

> 근거: review/fc9(전체), review/fc11(api/rules — startVoting 경계), unit-of-work.md U3
> 워크스페이스 루트: `/c/dev/tmp/ddd/Server`

## 구현 범위
- `meeting_place_pick` 도메인 + 담기/취소/목록/현황 API
- 호스트 투표 생성하기 → VOTING 전환 (session 생성은 U4)
- 담기 마감 스케줄러 (+3일, 후보 0개 시 U4에서 auto-candidate 처리)
- `meeting.pick_deadline` 컬럼 추가 (location phase 시작 시 now+3d 설정)

## 의존 (U1, U2 완료 전제)
- `MeetingRepository`, `GroupMemberRepository`, `MeetingParticipantRepository`
- `MeetingPlaceRecommendationRepository`, `PlaceRepository`
- `MemberRepository` (닉네임 조회)

---

## 생성/수정 파일 목록

### 신규 생성
```
meeting/domain/
  MeetingPlacePick.java
  MeetingPlacePickRepository.java

meeting/application/
  PlacePickService.java
  PlacePickSchedulerService.java

meeting/presentation/
  PlacePickController.java
  dto/PlaceCardResponse.java
  dto/PickStatusResponse.java
  dto/MemberPickStatus.java
  dto/StartVoteRequest.java

meeting/infrastructure/persistence/
  MeetingPlacePickJpaEntity.java
  MeetingPlacePickJpaRepository.java
  MeetingPlacePickRepositoryImpl.java

db/migration/V21__create_meeting_place_pick.sql
```

### 기존 수정
```
meeting/domain/Meeting.java                                  ← pickDeadline 필드 + completeRecommendation 수정
meeting/domain/MeetingRepository.java                        ← findRecommendedWithExpiredPickDeadline 추가
meeting/infrastructure/persistence/MeetingJpaEntity.java     ← pick_deadline 컬럼
meeting/infrastructure/persistence/MeetingJpaRepository.java ← 쿼리 추가
meeting/infrastructure/persistence/MeetingRepositoryImpl.java ← 메서드 구현
meeting/infrastructure/scheduler/MeetingScheduler.java        ← processExpiredPlacePicks 연결
```

---

## 실행 단계

### Step 1. Domain — MeetingPlacePick + Repository

- [x] 1.1 신규 `MeetingPlacePick` (id, meetingId, memberId, placeId, pickedAt) + `of()` factory
- [x] 1.2 신규 `MeetingPlacePickRepository` 인터페이스
  - save / existsByMeetingIdAndMemberIdAndPlaceId / deleteByMeetingIdAndMemberIdAndPlaceId
  - findByMeetingId / existsByMeetingId / countByMeetingIdAndMemberId
- [x] 1.3 `Meeting.java` 수정
  - `pickDeadline: LocalDateTime` 필드 + Builder 파라미터 추가
  - `completeRecommendation()` 수정: `this.pickDeadline = LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59).withNano(0)`
  - `isPickDeadlineExpired(): boolean` 추가
- [x] 1.4 `MeetingRepository` 수정 — `findRecommendedWithExpiredPickDeadline(LocalDateTime now)` 추가

### Step 2. Domain Unit Test

- [x] 2.1 `MeetingPlacePickTest` 신규 — `of()` 필드 확인
- [x] 2.2 `Meeting` 기존 테스트 클래스에 pickDeadline 케이스 추가
  - `completeRecommendation()` 호출 시 pickDeadline = now + 3d 23:59:59
  - `isPickDeadlineExpired()` 만료 전/후

### Step 3. Business Logic Summary

### Step 4. Application — PlacePickService

- [x] 4.1 신규 `PlacePickService`
  - 의존: MeetingRepository, GroupMemberRepository, MeetingParticipantRepository,
    MeetingPlacePickRepository, MeetingPlaceRecommendationRepository,
    PlaceRepository, MemberRepository

  - `getPlaces(meetingId, memberId, stationId, category, reservable, parking)` → List<PlaceCardResponse>
    - 멤버십 확인
    - recommendations → stationId 필터 → placeIds
    - placeRepository.findByIds(placeIds) → category/reservable/parking 필터
    - picks = findByMeetingId(meetingId) → pickCount/pickedByMe 계산
    - cardDistance = null (U4)

  - `pickPlace(meetingId, memberId, placeId)` @Transactional
    - 멤버십 확인, RECOMMENDED 확인, deadline 확인
    - 멱등: existsBy... true → return
    - save + 전원완료 체크 → auto toVoting

  - `cancelPick(meetingId, memberId, placeId)` @Transactional
    - 멤버십 확인, RECOMMENDED 확인, deadline 확인
    - deleteBy... (없으면 no-op)

  - `getPickStatus(meetingId, memberId)` → PickStatusResponse
    - 멤버십 확인 → participants + picks + memberNames 조합

  - `startVoting(meetingId, memberId, int durationDays)` @Transactional
    - 호스트 확인, RECOMMENDED 확인, existsByMeetingId 확인(후보 ≥ 1)
    - durationDays ∈ {1,3,7} → INVALID_DURATION_DAYS
    - deadline 검증: now+durationDays.toLocalDate() >= confirmedDate → PLACE_VOTE_DEADLINE_INVALID
    - meeting.toVoting() + save
    (U4에서 이 메서드에 MeetingPlaceVoteSession 생성 추가)

### Step 5. Application Unit Test

- [x] 5.1 `PlacePickServiceTest` 신규
  - getPlaces: stationId 필터, cardDistance null
  - pickPlace: 정상/멱등/LOCATION_NOT_RECOMMENDED/PLACE_PICK_CLOSED/전원완료 자동전환
  - cancelPick: 정상/PLACE_PICK_CLOSED/없는 pick no-op
  - startVoting: 정상/후보없음/durationDays유효성/deadline초과

### Step 6. API Layer Summary

### Step 7. Presentation — DTOs + Controller

- [x] 7.1 `PlaceCardResponse` (record): placeId, name, categoryLabel, address, vibes, cardDistance, pickCount, pickedByMe
- [x] 7.2 `MemberPickStatus` (record): memberId, nickname, done
- [x] 7.3 `PickStatusResponse` (record): members(List<MemberPickStatus>), myPicks(List<Long>)
- [x] 7.4 `StartVoteRequest` (record): durationDays(@NotNull)
- [x] 7.5 신규 `PlacePickController` (meeting.presentation)
  - `GET  /api/v1/meetings/{meetingId}/places`
  - `POST /api/v1/meetings/{meetingId}/places/{placeId}/pick` → 204
  - `DELETE /api/v1/meetings/{meetingId}/places/{placeId}/pick` → 204
  - `GET  /api/v1/meetings/{meetingId}/places/pick-status`
  - `POST /api/v1/meetings/{meetingId}/place-vote` → 200

### Step 8. API Layer Unit Testing

- [x] 8.1 스킵 (통합테스트 커버)

### Step 9. API Layer Summary

### Step 10. Repository Layer — Infrastructure

- [x] 10.1 `MeetingPlacePickJpaEntity` — @Table(meeting_place_pick), from/toDomain
- [x] 10.2 `MeetingPlacePickJpaRepository` extends JpaRepository — 필요 메서드 선언
- [x] 10.3 `MeetingPlacePickRepositoryImpl` — 전 메서드 구현
- [x] 10.4 `MeetingJpaEntity` 수정 — `pick_deadline TIMESTAMPTZ` 컬럼 추가, from/toDomain 반영
- [x] 10.5 `MeetingJpaRepository` 수정 — `findByLocationStatusAndPickDeadlineBefore` 추가
- [x] 10.6 `MeetingRepositoryImpl` 수정 — `findRecommendedWithExpiredPickDeadline` 구현

### Step 11. Repository Layer Unit Testing

- [x] 11.1 스킵 (통합테스트 커버)

### Step 12. Repository Layer Summary

### Step 13. Scheduler — PlacePickSchedulerService + MeetingScheduler 연결

- [x] 13.1 신규 `PlacePickSchedulerService` (application)
  - `processExpiredPickDeadlines()`:
    meetings = findRecommendedWithExpiredPickDeadline(now) → 각 meeting.toVoting() + save
    (0개 auto-candidate 처리는 U4 vote session 생성 시)

- [x] 13.2 `MeetingScheduler` 수정
  - `PlacePickSchedulerService` 주입
  - processScheduled()에서 processExpiredPickDeadlines() 호출 + try-catch

### Step 14. Database Migration Scripts

- [x] 14.1 `V21__create_meeting_place_pick.sql`
  - CREATE TABLE meeting_place_pick (id, meeting_id FK, member_id FK, place_id FK, picked_at)
  - UNIQUE(meeting_id, member_id, place_id)
  - ALTER TABLE meeting ADD COLUMN pick_deadline TIMESTAMPTZ

### Step 15. Documentation Generation

- [x] 15.1 `review/fc9/api.md` — 요청/응답 예시 보강

### Step 16. Deployment Artifacts

- [x] 16.1 해당 없음

---

## 주요 설계 결정

| 항목 | 결정 |
|---|---|
| cardDistance | null 반환 (U4 SubwayGraph 주입 후 채움) |
| 담기 마감 기준 | completeRecommendation() 시 now+3일 23:59:59 저장 |
| 0개 auto-candidate | U4 vote session 생성 시 처리 (U3 스케줄러는 VOTING 전환만) |
| startVoting 세션 생성 | U3: VOTING 전환만 / U4: 메서드 내 MeetingPlaceVoteSession 생성 추가 |
| 전원 담기완료 체크 | ABSENT 제외 participants × countByMeetingIdAndMemberId >= 1 |
| member_id FK | REFERENCES member(id) — auth.member 테이블 |
