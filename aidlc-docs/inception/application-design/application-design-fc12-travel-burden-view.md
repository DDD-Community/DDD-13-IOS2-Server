# Application Design — FC-12 보완: 친구들 거리보기 응답 보강

> 2026-06-25. requirements-fc12-travel-burden-view.md 기반. 단일 단위(Units SKIP).

## 1. 표현 계층 — `PlaceTravelBurdenResponse.MemberBurden` 확장

```
record MemberBurden(
    Long memberId,
    String name,            // 닉네임
    String departureName,   // 출발지 이름 (nullable)
    boolean isMe,           // 요청자 본인 여부
    Integer seconds,        // 소요초 (스냅샷 없으면 null)
    Integer transfers,      // 환승수 (스냅샷 없으면 null)
    boolean isLongest,      // 소요시간 보유 멤버 중 최대
    List<PathPoint> path    // 경로 (없으면 [])
)
```
- `PathPoint` 변경 없음.
- `seconds`/`transfers` 타입 int → **Integer**(nullable).

## 2. 애플리케이션 서비스 — `PlaceVoteService.getPlaceTravelBurden`

의존 추가: `DeparturePlaceRepository`.

흐름 (변경점 ⭐):
1. 모임 + 그룹원 검증 (동일)
2. ⭐ **활성 참여자(ABSENT 제외) 전원** 조회 → 멤버 목록의 기준.
3. 해당 장소 burden 스냅샷 로드 → `Map<memberId, MeetingTravelBurden>`.
4. 멤버 닉네임 배치 조회(`memberRepository.findAllById`).
5. ⭐ 멤버 출발지 배치 조회(`departurePlaceRepository.findAllByMemberIdIn`) → `Map<memberId, List<DeparturePlace>>`.
6. ⭐ 최장 소요시간 = burden 보유 멤버들 seconds 최대.
7. 참여자별 MemberBurden 조립:
   - burden 있으면 seconds/transfers/path, 없으면 null/null/[].
   - isLongest = (seconds != null && seconds == maxSec).
   - isMe = participant.memberId == 요청자.
   - departureName = ⭐ `resolveDepartureName(participant, places)`.
8. place 정보(PlaceSummary) + 멤버 리스트 응답.

### `resolveDepartureName` (private)
- 멤버의 DeparturePlace 중 **좌표가 참여자 좌표와 일치**(epsilon 1e-6)하는 것 → `placeName != null ? placeName : label`.
- 없으면 `isDefault==true` 출발지의 이름.
- 그래도 없으면 null.

## 3. 변경 없음
- 스냅샷 계산(`computeAndSaveTravelBurdens`) 변경 없음.
- `getVoteStatus` 변경 없음.
- 스키마/마이그레이션 없음.

## 4. 영향 파일
- 수정: `PlaceTravelBurdenResponse.java`, `PlaceVoteService.java`(의존 + getPlaceTravelBurden)
- 테스트: `PlaceVoteServiceTest`(거리보기) — 참여자 전원/ departureName/isMe/null seconds 검증.
