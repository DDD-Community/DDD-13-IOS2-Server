# Application Design — FC-12: 출발지 메타 저장 + 장소투표 참여 팀원 조회

## 1. 변경 개요
| 레이어 | 대상 | 변경 |
|---|---|---|
| DB | `meeting_participant` | V30 — `departure_label`, `departure_place_name`, `departure_address` 추가 + 백필 |
| domain | `MeetingParticipant` | 메타 필드 3종 + `create`/`updateDeparture` 시그니처 확장 + `departureName()` 헬퍼 |
| infra | `MeetingParticipantJpaEntity` | 컬럼 매핑 + `from`/`toDomain`/`updateDeparture` 갱신 |
| application | `GroupService`, `GroupInviteService`, `PlaceSelectionService` | 쓰기 시 메타 전달 |
| application | `PlaceVoteService` | `getPlaceTravelBurden` 리팩터(역매칭 제거) + 신규 `getVoteParticipants` |
| presentation | `PlaceVoteController` | 신규 엔드포인트 |
| presentation | DTO | 신규 `VoteParticipantsResponse` |

## 2. DB — V30
```sql
-- V30__add_meeting_participant_departure_meta.sql
ALTER TABLE meeting_participant
    ADD COLUMN departure_label       VARCHAR(100),
    ADD COLUMN departure_place_name  VARCHAR(150),
    ADD COLUMN departure_address     VARCHAR(255);

-- best-effort 백필: 회원 기본 출발지 기준
UPDATE meeting_participant mp
SET departure_label      = dp.label,
    departure_place_name = dp.place_name,
    departure_address    = COALESCE(dp.road_address, dp.address)
FROM departure_place dp
WHERE dp.member_id = mp.member_id
  AND dp.is_default = true
  AND mp.departure_label IS NULL;
```
> 컬럼명/테이블명은 기존 `departure_place` 실제 스키마(place_name/road_address/address/is_default) 확인 후 확정.

## 3. 도메인 — MeetingParticipant
```java
private String departureLabel;
private String departurePlaceName;
private String departureAddress;

public static MeetingParticipant create(Long meetingId, Long memberId,
        Double latitude, Double longitude, String attendanceStatus,
        String departureLabel, String departurePlaceName, String departureAddress) { ... }

public void updateDeparture(double latitude, double longitude,
        String departureLabel, String departurePlaceName, String departureAddress) {
    this.latitude = latitude; this.longitude = longitude;
    this.departureLabel = departureLabel;
    this.departurePlaceName = departurePlaceName;
    this.departureAddress = departureAddress;
}

/** 표시명: 카카오 장소명 우선, 없으면 사용자 별칭, 둘 다 없으면 null */
public String departureName() {
    return departurePlaceName != null ? departurePlaceName : departureLabel;
}
```
- 하위호환: 기존 `create(...5인자)` 호출부 없음(전 호출부 수정). 단일 시그니처로 통일.

## 4. 쓰기 경로 어댑터
공통 추출 헬퍼(각 서비스 내 또는 DeparturePlace에 메서드):
- `label = departure.getLabel()`
- `placeName = departure.getPlaceName()`
- `address = departure.getRoadAddress() != null ? roadAddress : departure.getAddress()`

1. **GroupService.createGroup**: `findDefaultByMemberId` → 메타 추출 → `create(...8인자)`. 출발지 없으면 좌표/메타 모두 null.
2. **GroupInviteService.createMeetingParticipant**: 동일 패턴.
3. **PlaceSelectionService.updateParticipantDeparture**: `findByIdAndMemberId`로 선택 출발지 → `participant.updateDeparture(lat,lng,label,placeName,address)`.

## 5. 읽기 리팩터 — getPlaceTravelBurden
- 제거: `resolveDepartureName`, `coordMatches`, `departureLabel`, `departurePlaceRepository` 의존(이 메서드 한정).
- 변경: `String departureName = p.departureName();` 직접 사용.
- 효과: `departure_place` 추가 조회/좌표매칭 삭제 → 단순화.

## 6. 신규 — PlaceVoteService.getVoteParticipants
```
getVoteParticipants(meetingId, memberId) -> VoteParticipantsResponse
```
흐름:
1. 모임 조회(`MEETING_NOT_FOUND`)
2. 그룹원 검증(`NOT_GROUP_MEMBER`)
3. `locationStatus != VOTING` → `PLACE_VOTE_NOT_IN_PROGRESS`
4. 세션 조회(`PLACE_VOTE_NOT_IN_PROGRESS`)
5. 활성 참여자(ABSENT 제외) 조회
6. `voteRepository.findBySessionId` → distinct `voterIds`
7. 활성 memberIds → `memberRepository.findAllById` 배치 → nickname/profileImageUrl
8. 참여자별 DTO 조립:
   - name = member.nickname (null이면 "")
   - profileImageUrl = member.profileImageUrl (원본 key)
   - departureName = participant.departureName()
   - isMe = memberId.equals(p.memberId)
   - voted = voterIds.contains(p.memberId)
9. 정렬: participant 조회 순서 유지

## 7. DTO
```java
public record VoteParticipantsResponse(List<Participant> participants) {
    public record Participant(
        Long memberId,
        String name,
        String profileImageUrl,
        String departureName,
        boolean isMe,
        boolean voted
    ) {}
}
```

## 8. Controller
```java
@Operation(summary = "장소투표 참여 팀원 조회 — 활성 참여자별 이름·프로필·출발지·투표여부")
@GetMapping("/{meetingId}/place-vote/participants")
public VoteParticipantsResponse getVoteParticipants(@PathVariable Long meetingId,
        Authentication authentication) {
    Long memberId = Long.parseLong(authentication.getName());
    return placeVoteService.getVoteParticipants(meetingId, memberId);
}
```

## 9. 영향/리스크
- **테스트 영향**: `MeetingParticipant.create` 시그니처 변경 → 기존 테스트 빌더 호출 수정 필요(PlaceVoteServiceTest 등).
- **마이그레이션**: V30 백필은 best-effort. 좌표는 바꾸지 않음(이름만 보강).
- **순서**: 스키마/도메인/쓰기경로 먼저 → 읽기 리팩터 → 신규 API → 테스트.
