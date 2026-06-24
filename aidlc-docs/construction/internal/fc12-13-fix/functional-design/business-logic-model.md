# Business Logic Model — FC-12/13 보완

## 메서드별 변경 명세

> **사전 정정 사항** (Opus 재검토)
> - **마이그레이션 = V28** (V26/V27 이미 존재)
> - **Member** = `com.bangawo.auth.domain.Member` (`getNickname()`), `MemberRepository.findAllById`
> - getVoteStatus의 `memberStatuses`는 **전원 공개**(2026-06-24 변경) → 호스트 분기·role 판단 불필요
> - 신규 ErrorCode `PLACE_VOTE_INVALID_CANDIDATE` = **MEETING_023** (022까지 사용 중)

### 1. PlaceVoteService.startVoting (수정)

```
현재:
  picks 없으면 → LOCATION_NOT_RECOMMENDED 예외

변경:
  RECOMMENDED 상태 체크 유지
  if voteSession 이미 있으면 → PLACE_VOTE_ALREADY_STARTED
  durationDays 유효성 체크 유지
  마감일 유효성 체크 유지
  ──────────────────────────────────
  [삭제] picks 없으면 LOCATION_NOT_RECOMMENDED 던지던 가드 제거 (담기 0개여도 진행)
  meeting.toVoting()
  meetingRepository.save(meeting)
  createSession(meetingId, durationDays)
       └─ 내부: backfillCandidatesIfNeeded(meetingId) → 세션 생성 → 이동부담 스냅샷
```

> startVoting은 백필을 직접 호출하지 않음 — createSession이 단일 진입점에서 처리(#6 참조).

### 2. PlaceVoteService.backfillCandidatesIfNeeded (신규 private, createSession 진입부 호출)

```
picks ← meetingPlacePickRepository.findByMeetingId(meetingId)
distinctPlaceIds ← picks.placeId.stream().distinct().collect(toSet())

if distinctPlaceIds.size() < 3:
    recs ← recommendationRepository.findByMeetingIdOrderByRank(meetingId)
    for rec in recs:
        if rec.placeId not in distinctPlaceIds:
            meetingPlacePickRepository.save(
                MeetingPlacePick.ofSystem(meetingId, rec.placeId)
            )
            distinctPlaceIds.add(rec.placeId)
        if distinctPlaceIds.size() >= 3: break
    // 추천 총량이 3 미만이면 가능한 만큼만 (루프 종료)
```

### 3. PlaceVoteService.submitVote (수정)

```
현재:
  후보집합 ← recommendations (추천15개)
  candidateCount = recommendations.size()
  maxVotes = max(1, candidateCount/2)
  placeId 소속 검증 없음

변경:
  후보집합 ← meetingPlacePickRepository.findByMeetingId(meetingId)
              .stream().map(getPlaceId).distinct().collect(toSet())
  candidateCount = candidatePlaceIds.size()
  maxVotes = max(1, candidateCount / 2)

  if placeIds.size() > maxVotes → PLACE_VOTE_LIMIT_EXCEEDED

  [신규] placeIds 중 후보집합에 없는 것이 있으면 → PLACE_VOTE_INVALID_CANDIDATE

  나머지 기존 로직 유지(삭제→저장→전원투표 체크)
```

### 4. PlaceVoteService.getVoteStatus (수정)

```
현재:
  candidates 소스 ← recommendations
  정렬 없음
  memberStatuses 없음

변경:
  picks ← meetingPlacePickRepository.findByMeetingId(meetingId)
  candidatePlaceIds ← picks.distinct().placeId 목록
  placeById ← placeRepository.findByIds(candidatePlaceIds) → Map<Long,Place>

  // 정렬
  boolean voted = myVotedPlaceIds.size() > 0
  if !voted: candidatePlaceIds.sort by place.name 가나다 ASC
  else:      candidatePlaceIds.sort by voteCount DESC, then name ASC

  candidates 조립 (기존 CandidateVoteInfo 구조 유지)

  // 🔄 전원 공개 — 호스트 분기 제거, 모든 호출자에게 memberStatuses 제공
  // 기준 집합 = 활성 참여자(ABSENT 제외) — votedCount 계산의 activeIds 재사용
  Map<Long,Member> memberById = memberRepository.findAllById(activeIds)
                                   .stream().collect(toMap(Member::getId, m->m))
  List<MemberVoteStatus> memberStatuses = participants.stream()
      .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
      .map(p -> {
          String name = memberById.get(p.getMemberId()) != null
                          ? memberById.get(p.getMemberId()).getNickname() : ""
          boolean completed = voterIds.contains(p.getMemberId())
          return new MemberVoteStatus(p.getMemberId(), name, completed)
      }).toList()

  return PlaceVoteStatusResponse(... memberStatuses ...)
```

### 5. PlaceVoteService.computeAndSaveTravelBurdens (수정)

```
현재:
  recommendations ← recommendationRepository.findByMeetingIdOrderByRank(meetingId)
  for rec in recommendations: destStation = rec.getNearestStationId()

변경:
  picks ← meetingPlacePickRepository.findByMeetingId(meetingId)
  candidatePlaceIds ← picks.distinct().placeId
  
  // placeId → nearestStationId 매핑은 recommendation 테이블에서 조회
  // (MeetingPlaceRecommendation이 nearestStationId 보유)
  recMap ← recommendationRepository.findByMeetingIdOrderByRank(meetingId)
              .stream().collect(toMap(placeId → rec))
  
  for placeId in candidatePlaceIds:
      rec = recMap.get(placeId)  // 백필 SYSTEM 장소도 추천에 있으면 역 매핑 가능
      if rec == null or rec.nearestStationId == null: skip
      // 이하 기존 다익스트라 로직 동일
```

> **주의**: SYSTEM 백필된 장소는 recommendation에 반드시 존재함(백필 소스가 recommendation이므로). 안전.

### 6. PlacePickSchedulerService.processExpiredPickDeadlines (수정)

```
변경 없음(호출부) — createSessionWithDefaultDuration 내부에 백필 포함으로 통합:
  meeting.toVoting()
  meetingRepository.save(meeting)
  placeVoteService.createSessionWithDefaultDuration(meeting.getId())
       └─ 내부: backfillCandidatesIfNeeded(meetingId) → createSession(...)
```

> **설계 결정 (백필 단일 진입점)**: 백필을 `createSession` **직전**에 수행하도록
> `createSession(meetingId, durationDays)` 진입부에 `backfillCandidatesIfNeeded(meetingId)` 를 둔다.
> 이렇게 하면 호스트 수동 `startVoting`, 마감 자동 `createSessionWithDefaultDuration`,
> 전원 담기 트리거 등 **모든 세션 생성 경로가 단일 백필 지점**을 공유 → 누락 위험 제거.
> (startVoting에서 별도 백필 호출 불필요 — createSession이 담당)

### 7. PlaceConfirmService.confirmPlace (수정)

```
현재:
  pickOrderByPlaceId ← 삽입 순서 인덱스(0,1,2...) → 4순위 tiebreaker
  Comparator: -pickOrderByPlaceId (작은 인덱스 = 먼저 담음 = 우선)

변경:
  firstPickedAtByPlaceId ← picks.stream()
      .collect(groupingBy(placeId, minBy(pickedAt)))
                                     ↑ LocalDateTime 기준
  Comparator: firstPickedAtByPlaceId(placeId) ASC (빠를수록 우선)

  전체 Comparator(공통):
  Comparator<Long> candidateComparator =
      Comparator.comparingLong((Long id) -> voteCount.getOrDefault(id, 0L)).reversed()
          .thenComparingLong(id -> secondsSum.getOrDefault(id, 0L))
          .thenComparingLong(id -> transfersSum.getOrDefault(id, 0L))
          .thenComparing(id -> firstPickedAtByPlaceId.getOrDefault(id, LocalDateTime.MAX))

  sorted(candidateComparator) → 1위 = 첫 번째
```

### 8. PlaceConfirmService.getResult (수정)

```
현재:
  candidates 정렬 없음 (삽입 순서)
  rank 필드 없음

변경:
  candidates ← candidatePlaceIds 기준 동일 공통 비교자로 정렬
  rank 부여: 상위 3개에 1,2,3 / 나머지는 0
  CandidateResult 에 rank 포함
```

### 9. PlaceConfirmService.confirmByHost (신규)

```
호스트 검증: groupMemberRepository → NOT_GROUP_HOST
상태 검증: meeting.locationStatus == VOTING → else PLACE_VOTE_NOT_IN_PROGRESS
meeting.toConfirmed()
meetingRepository.save(meeting)
confirmPlace(meetingId)
```

### 9-2. PlaceVoteService.getPlaceTravelBurden (신규) — 친구들 거리보기

```
입력: meetingId, placeId, memberId(호출자)
1. meeting 조회 + groupMemberRepository 구성원 검증
2. burdens ← travelBurdenRepository.findByMeetingId(meetingId)
              .stream().filter(b -> placeId.equals(b.getPlaceId())).toList()
3. maxSec ← burdens.stream().mapToLong(seconds).max().orElse(0)
4. memberById ← memberRepository.findAllById(burdens.memberId 집합) → nickname
5. place ← placeRepository.findByIds([placeId]).get(0) → PlaceSummary.from
6. return PlaceTravelBurdenResponse(place, burdens.map(b ->
       new MemberBurden(b.memberId, nickname, b.seconds, b.transfers, b.seconds==maxSec)))
```

신규 DTO:
```
record PlaceTravelBurdenResponse(PlaceSummary place, List<MemberBurden> burdens)
record MemberBurden(Long memberId, String name, int seconds, int transfers, boolean isLongest)
```

신규 컨트롤러 메서드(PlaceVoteController):
```
@GetMapping("/{meetingId}/place-vote/{placeId}/travel-burden")
getPlaceTravelBurden(meetingId, placeId, authentication) → placeVoteService.getPlaceTravelBurden(...)
```

### 10. ErrorCode 추가

```java
PLACE_VOTE_INVALID_CANDIDATE(HttpStatus.BAD_REQUEST, "MEETING_023", "후보에 없는 장소에 투표할 수 없습니다")
```

## 공통 비교자 공유 전략

`confirmPlace` 내부 로직과 `getResult` 조회 로직에서 **동일한 Comparator 생성 코드**를 사용.

선택 방안: **private 헬퍼 메서드** `buildCandidateComparator(picks, voteCount, secondsSum, transfersSum)` 를 `PlaceConfirmService` 내부에 정의 → 두 메서드에서 호출.

도메인 서비스(별도 클래스) 분리는 YAGNI 원칙상 미적용 (단일 클래스 내에서 충분).

## 레이어 의존 방향 준수 확인

```
presentation (PlaceVoteController 수동확정 엔드포인트 추가)
    ↓
application (PlaceVoteService 수정, PlaceConfirmService 수정/추가)
    ↓
domain (MeetingPlacePick 수정, PickSource 추가, Repository 인터페이스 확장)
    ↑
infrastructure (MeetingPlacePickJpaEntity 수정, RepositoryImpl 수정)
```

- domain은 JPA/Spring에 의존 없음 유지
- application이 domain 인터페이스만 참조
- infrastructure가 domain 인터페이스 구현
