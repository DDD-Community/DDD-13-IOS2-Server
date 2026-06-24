# Business Rules — FC-12/13 보완

## R0. 공통 — 투표 후보 집합 정의 (수정)

| 규칙 | 내용 |
|---|---|
| 후보 소스 | `meeting_place_pick` 전체 행 (USER + SYSTEM, distinct placeId) |
| 이전 오구현 | `meeting_place_recommendation` (추천 15개) — 폐기 |
| 적용 범위 | 투표 시작·제출·현황 조회·이동부담 계산·확정 모두 동일 후보 집합 |

## R1. 후보 백필 — 투표 시작 시 최소 3개 보장

**트리거**: startVoting(호스트 수동) / processExpiredPickDeadlines(마감 자동전환) — 세션 생성 직전

```
후보집합 ← meetingPlacePickRepository.findDistinctPlaceIdsByMeetingId(meetingId)

if 후보집합.size < 3:
    추천목록 ← recommendationRepository.findByMeetingIdOrderByRank(meetingId)  // rank ASC
    for rec in 추천목록:
        if rec.placeId not in 후보집합:
            meetingPlacePickRepository.save(MeetingPlacePick.ofSystem(meetingId, rec.placeId))
            후보집합.add(rec.placeId)
        if 후보집합.size >= 3: break
    // 추천 총량이 3 미만이면 가능한 만큼만
```

- 백필 후에도 후보 0개 → 세션 생성은 계속 진행 (0개 멈춤 제거, 전원기권→등록순 확정)
- 백필은 **트랜잭션 내** 진행 (세션 생성과 원자적)

**호스트 수동 startVoting 변경점**:
- 기존: `existsByMeetingId == false → LOCATION_NOT_RECOMMENDED` 예외
- 변경: 백필 수행 후 세션 생성 (담기 0개여도 에러 없음)

## R2. 투표 제출 검증 (수정)

### 후보수 기반 다중 제한
- maxVotes = max(1, 후보집합.size / 2) (내림)
- 후보 집합 소스 = meeting_place_pick(전체, distinct)

### placeId 소속 검증 (신규)
- 제출된 placeId **모두**가 현재 후보집합에 속해야 함
- 위반 시 → `PLACE_VOTE_INVALID_CANDIDATE(400)`

### 에러 우선순위
1. 상태 VOTING 아님 → `PLACE_VOTE_NOT_IN_PROGRESS(400)`
2. placeIds.size > maxVotes → `PLACE_VOTE_LIMIT_EXCEEDED(400)`
3. placeId 후보 미소속 → `PLACE_VOTE_INVALID_CANDIDATE(400)`

## R3. 투표 현황 조회 정렬 (신규)

| 상태 | 정렬 기준 |
|---|---|
| 내 투표 0개 (미투표) | 상호명(place.name) 가나다순 (오름차순) |
| 내 투표 1개 이상 (투표 완료) | 득표수 내림차순, 동점 시 가나다순 |

- "내 투표" 판단 = 현재 `memberId`가 제출한 투표 수 (`myVotedPlaceIds.size()`)
- 후보 목록 = picks(USER+SYSTEM) distinct → place 정보 조인

## R4. 멤버별 투표 참여 현황 (전원 공개, 신규)

> 🔄 2026-06-24 결정 변경: 호스트 전용 → **모든 구성원 공개**.
> 기준 집합은 **모임 참여자(meeting_participant, ABSENT 제외)** — 기존 `votedCount`/`totalActive`
> 계산 집합과 동일. 기존 `PlacePickService.getPickStatus`(담기 현황 전원 공개) 패턴과 정합.

- **모든 호출자**(호스트/일반 구성원 무관) 조회 시 `memberStatuses` 리스트 포함 (호스트 분기 없음)
  - 대상: 모임 활성 참여자(ABSENT 제외) 전원
  - 완료 여부: 해당 참여자가 현재 세션에 1개 이상 투표 했으면 `completed=true`
  - 이름(`name`)은 `MemberRepository.findAllById` 조회 후 `getNickname()`
- **완료 정의**: 투표 0개 되면 미완료 (재투표로 완료 취소 가능)
- **익명성**: 완료여부만 노출 — 어떤 후보에 투표했는지는 **누구에게도** 비공개
- 총 참여율(`totalParticipants`/`votedCount`)도 기존대로 함께 제공

## R5. 공통 순위 비교자 (수정)

모든 확정 경로(자동/마감/호스트수동)와 결과 조회가 **단일 비교자**를 공유한다.

```
비교 우선순위:
1. 득표수 (voteCount) 내림차순
2. 동점 → 이동시간합 (totalSeconds) 오름차순
3. 동점 → 환승합 (totalTransfers) 오름차순
4. 동점 → 최초 담은 시각 (min pickedAt, USER+SYSTEM 무관) 오름차순
```

**4순위 산출**:
```
Map<Long, LocalDateTime> firstPickedAtByPlaceId =
    picks.stream().collect(
        groupingBy(MeetingPlacePick::getPlaceId,
            collectingAndThen(minBy(Comparator.comparing(MeetingPlacePick::getPickedAt)),
                opt -> opt.map(MeetingPlacePick::getPickedAt).orElse(LocalDateTime.MAX)))
    );
```

**이전 오구현**: `pickOrderByPlaceId` (삽입 순서 인덱스 long) → 제거  
**변경**: min(pickedAt) LocalDateTime 비교로 대체

## R6. 1~3위 산출 (신규)

- `getResult` 응답의 각 `CandidateResult`에 `rank` 포함
- 1~3위 = 공통 비교자로 정렬된 후보 중 상위 3개에 rank 1, 2, 3 부여
- 후보 < 3이면 후보 수만큼만 rank 부여 (rank 1만, 또는 rank 1, 2만)
- rank 미부여 후보(4위 이하)도 candidates에 포함, `rank = 0` (null 대신 0으로 표현)

## R7. 수동 확정 (신규)

**엔드포인트**: `POST /api/v1/meetings/{meetingId}/place-confirm`

```
1. 호스트 검증 (NOT_GROUP_HOST)
2. locationStatus == VOTING 검증 (PLACE_VOTE_NOT_IN_PROGRESS)
3. meeting.toConfirmed()
4. confirmPlace(meetingId) 공통 로직 실행
```

- 공통 비교자 동일 적용 (자동 확정과 동일 코드)
- 이미 CONFIRMED이면 → `PLACE_VOTE_NOT_IN_PROGRESS(400)`

## R8. 이동부담 계산 대상 (수정)

- 기존: `recommendationRepository` (추천15개) 기준으로 계산
- 변경: `meetingPlacePickRepository.findDistinctPlaceIdsByMeetingId` (picks 집합) 기준으로 계산
- 백필 후 세션 생성이므로 computeAndSaveTravelBurdens 호출 시점엔 백필 완료 상태

## R10. 친구들 거리보기 — 단일 장소 이동부담 (신규)

**엔드포인트**: `GET /api/v1/meetings/{meetingId}/place-vote/{placeId}/travel-burden`

```
1. 모임 조회 + 구성원 검증 (NOT_GROUP_MEMBER)
2. burdens ← travelBurdenRepository.findByMeetingId(meetingId)
              .filter(b -> b.placeId == placeId)
3. maxSeconds ← burdens.max(seconds)  // isLongest 판정용
4. memberId → nickname 매핑 (MemberRepository.findAllById)
5. 응답: place(PlaceSummary) + burdens[{memberId, name, seconds, transfers, isLongest}]
```

- 데이터 소스 = `meeting_travel_burden` 스냅샷(투표 시작 시 저장분). **신규 다익스트라 계산 없음**.
- 스냅샷 없음(그래프 미로드) → `burdens = []` 빈 목록.
- 권한: 모임 구성원(getVoteStatus와 동일).

## R9. 집계 분리 원칙

| 용도 | 기준 |
|---|---|
| 투표/확정 후보 집합 | `meeting_place_pick` 전체 (USER+SYSTEM) |
| 담기 현황·함께담기 N | `source='USER'` 만 |
| 구성원 완료현황(호스트용) | `source='USER'` 담기자 == 모임 참여자 기준 |
| 이동부담 계산 대상 | picks(USER+SYSTEM) distinct placeId |
