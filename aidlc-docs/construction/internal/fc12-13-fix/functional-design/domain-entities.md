# Domain Entities — FC-12/13 보완

## 변경 대상 엔티티

### 1. MeetingPlacePick (수정)

**현재 상태**
- 필드: `id`, `meetingId`, `memberId(NOT NULL)`, `placeId`, `pickedAt`
- 팩토리: `of(meetingId, memberId, placeId)` 1종

**변경 사항**
- `source` 필드 추가 (enum `PickSource`: USER | SYSTEM)
  - USER: 구성원이 직접 담은 장소
  - SYSTEM: 백필로 자동 추가된 장소
- `memberId` → nullable 허용 (SYSTEM 행은 NULL)
- `ofSystem(meetingId, placeId)` 팩토리 추가
  - `memberId=null`, `source=SYSTEM`, `pickedAt=now()`

> ⚠️ **마이그레이션 버전 정정**: AD/review 문서는 `V26` 으로 가정했으나 실제 워크스페이스에
> `V26__date_with_time.sql`, `V27__add_midpoint_station_candidate_location.sql` 이 이미 존재.
> **신규 마이그레이션 = `V28`** 로 작성한다. (review/erd 문서의 V26 표기는 코드젠 단계에서 V28로 동기화)

```
MeetingPlacePick
├── Long id
├── Long meetingId
├── Long memberId          ← nullable (SYSTEM pick은 NULL)
├── Long placeId
├── LocalDateTime pickedAt
└── PickSource source      ← 신규 (USER | SYSTEM)

팩토리
├── of(meetingId, memberId, placeId)       → source=USER
└── ofSystem(meetingId, placeId)           → source=SYSTEM, memberId=null
```

### 2. PickSource (신규 enum)

```
enum PickSource {
    USER,    // 구성원이 직접 담음
    SYSTEM   // 백필(추천 자동 추가)
}
```

**위치**: `com.bangawo.meeting.domain.PickSource`

### 3. MeetingPlacePickRepository (인터페이스 확장)

기존 메서드 유지. 신규 추가:

```
// 백필 전 후보 집합 확인용 (USER+SYSTEM 모두 포함)
List<Long> findDistinctPlaceIdsByMeetingId(Long meetingId);

// 백필 행 배치 저장
List<MeetingPlacePick> saveAll(List<MeetingPlacePick> picks);
```

> `findByMeetingId` 결과에서 stream().distinct()로도 가능하나,
> 명시적 메서드가 의도를 명확히 함.

### 4. PlaceResultResponse.CandidateResult (수정)

```
// 기존
record CandidateResult(PlaceSummary place, int voteCount, long totalSeconds, long totalTransfers)

// 변경 후
record CandidateResult(int rank, PlaceSummary place, int voteCount, long totalSeconds, long totalTransfers)
```

### 5. PlaceVoteStatusResponse (수정)

```
// 기존
record PlaceVoteStatusResponse(
    LocalDateTime deadline, String sessionStatus,
    int totalParticipants, int votedCount,
    List<CandidateVoteInfo> candidates
)

// 변경 후: memberStatuses 필드 추가
record PlaceVoteStatusResponse(
    LocalDateTime deadline, String sessionStatus,
    int totalParticipants, int votedCount,
    List<MemberVoteStatus> memberStatuses,   ← 신규 (호스트 응답에만 채워짐, 구성원은 null)
    List<CandidateVoteInfo> candidates
)

// 신규 nested record (review/fc12/api.md 계약: 필드명 name, nickname으로 채움)
record MemberVoteStatus(Long memberId, String name, boolean completed)
```

> **이름 소스**: `com.bangawo.auth.domain.Member.getNickname()` (Member는 auth 컨텍스트).
> `MemberRepository.findAllById(memberIds)` 로 일괄 조회 (기존 PlacePickService.getPickStatus 패턴과 동일).

## 변경되지 않는 엔티티

| 엔티티 | 이유 |
|---|---|
| `MeetingPlaceVoteSession` | 세션 구조 변경 없음 |
| `MeetingPlaceVote` | 투표 레코드 구조 변경 없음 |
| `MeetingTravelBurden` | 스냅샷 구조 변경 없음 |
| `MeetingConfirmedPlace` | 저장 구조 변경 없음 (rank는 조회 시 계산) |
| `MeetingPlaceRecommendation` | 백필 소스로 조회만, 구조 변경 없음 |
| `Meeting` | 상태 전이 메서드 변경 없음 |

## V28 스키마 변경 (Flyway) — 버전 정정 (V26 아님)

```sql
-- V28__place_pick_source_backfill.sql
ALTER TABLE meeting_place_pick
    ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'USER';

ALTER TABLE meeting_place_pick
    ALTER COLUMN member_id DROP NOT NULL;

-- 기존 UNIQUE 제약: (meeting_id, member_id, place_id)
-- SYSTEM 행은 member_id=NULL → 동일 placeId를 중복 방지하려면 partial unique 적용
-- 단순화: SYSTEM 행은 삽입 전 existsByMeetingIdAndPlaceId 검사로 중복 방지 (앱 레벨)
```

> **UNIQUE 제약 주의**: 기존 제약 `(meeting_id, member_id, place_id)` 는
> member_id가 NULL이면 복수 NULL 허용(표준 SQL). SYSTEM 백필은 삽입 전 `source='SYSTEM'`
> AND `placeId` 존재 여부를 앱 레벨에서 체크하여 중복 방지.
