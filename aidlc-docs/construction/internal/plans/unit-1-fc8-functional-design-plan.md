# U1 기반 — Functional Design Plan (확정)

> 범위: LocationStatus 4-state 교체 + Meeting 가드, 그룹/모임 생성 흐름 확장(categoryLabels/vibes), 신규 ErrorCode, V18 마이그레이션
> review 매핑: `review/fc4`(생성 확장, flow.md 생략 합의됨), `review/fc8`(상태/가드)
> 에러코드/전이조건은 review/fc8~fc13에 이미 정의된 값을 그대로 재사용 (신규 발명 없음)

## 체크리스트

- [x] LocationStatus 4-state 정의 (BEFORE/RECOMMENDED/VOTING/CONFIRMED)
- [x] Meeting 도메인 가드 메서드 설계
- [x] computeListStatus 로직 갱신
- [x] CreateGroupRequest/CreateMeetingRequest에 categoryLabels/vibes 추가
- [x] Meeting.create / GroupService 시그니처 확장 설계
- [x] 신규 ErrorCode 정의 (review/fc8~fc13 기준 그대로)
- [x] V18 마이그레이션 설계
- [x] V26 마이그레이션 — 지금 생성 안 함, U5 이후 마지막에 생성 (결정만 기록)

## 1. LocationStatus 4-state
```java
public enum LocationStatus {
    BEFORE, RECOMMENDED, VOTING, CONFIRMED
}
```
기존 `IN_PROGRESS`, `COMPLETED` 제거.

## 2. Meeting 가드 메서드
fc8 flow.md(가드 체크 → ... → RECOMMENDED 저장)와 일치시키기 위해 가드/전환 분리.

```java
// 가드만 체크, 상태 변경 없음 — U2의 location/start 진입 시 호출
public void assertCanStartLocationPhase() {
    if (dateVoteStatus != DateVoteStatus.COMPLETED) {
        throw new BusinessException(ErrorCode.PLACE_PHASE_NOT_READY);
    }
    if (locationStatus != LocationStatus.BEFORE) {
        throw new BusinessException(ErrorCode.LOCATION_PHASE_ALREADY_STARTED);
    }
}

// 추천 계산 완료 후 전환 — U2가 top15 스냅샷 저장 후 호출
public void completeRecommendation() {
    this.locationStatus = LocationStatus.RECOMMENDED;
    this.updatedAt = LocalDateTime.now();
}

// U3(전원 담기완료/담기마감/호스트 투표생성) 또는 U4에서 호출
public void toVoting() {
    if (locationStatus != LocationStatus.RECOMMENDED) {
        throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
    }
    this.locationStatus = LocationStatus.VOTING;
    this.updatedAt = LocalDateTime.now();
}

// U5(전원 투표완료/투표마감)에서 호출
public void toConfirmed() {
    if (locationStatus != LocationStatus.VOTING) {
        throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
    }
    this.locationStatus = LocationStatus.CONFIRMED;
    this.updatedAt = LocalDateTime.now();
}
```
`LOCATION_NOT_RECOMMENDED`(review/fc9,fc11), `PLACE_VOTE_NOT_IN_PROGRESS`(review/fc12)는 신규 추가 코드, 명칭은 review 문서 그대로.

## 3. computeListStatus 갱신
- `locationStatus == CONFIRMED && dateVoteStatus == COMPLETED` → `CONFIRMED`
- 그 외 → `IN_PROGRESS` (CLOSED 조건 동일 유지)

## 4. 그룹/모임 생성 흐름 확장
- `CreateGroupRequest`/`CreateMeetingRequest`: `categoryLabels: List<String>`, `vibes: List<String>` 추가(둘 다 선택, 값 검증은 차후 application 레이어)
- `Meeting.create(groupId, name, themeTagCode, categoryLabels, vibes)` 시그니처 확장
- `GroupService.createGroupWithMeeting` / `createNextMeeting` 파라미터 확장 → `Meeting.create`에 전달
- 그룹 테이블엔 저장 안 함 (meeting 테이블에만 저장)

## 5. 신규 ErrorCode (review/fc8~fc13 그대로, MEETING_xxx 다음 번호로 추가)
| 코드 | 메시지 | 출처 |
|---|---|---|
| PLACE_PHASE_NOT_READY | 날짜가 아직 확정되지 않았습니다 | fc8 |
| PLACE_RECOMMENDATION_EMPTY | 추천 가능한 장소가 없습니다 | fc8 |
| LOCATION_NOT_RECOMMENDED | 장소 추천 단계가 아닙니다 | fc9/fc11 |
| PLACE_PICK_CLOSED | 담기가 마감되었습니다 | fc9 |
| PLACE_VOTE_DEADLINE_INVALID | 투표 마감일은 약속 날짜 이전으로 설정해 주세요 | fc11 |
| PLACE_VOTE_NOT_IN_PROGRESS | 투표가 진행 중이 아닙니다 | fc12 |
| PLACE_VOTE_LIMIT_EXCEEDED | 투표 가능 개수를 초과했습니다 | fc12 |
| PLACE_NOT_CONFIRMED | 아직 장소가 확정되지 않았습니다 | fc13 |

기존 재사용(추가 안 함): `LOCATION_PHASE_ALREADY_STARTED`, `PARTICIPANT_DEPARTURE_NOT_SET`, `MIDPOINT_STATION_NOT_FOUND`, `INVALID_DURATION_DAYS`, `NOT_GROUP_HOST`.

## 6. V18 마이그레이션
```sql
ALTER TABLE meeting ADD COLUMN category_labels TEXT[];
ALTER TABLE meeting ADD COLUMN vibes TEXT[];
COMMENT ON COLUMN meeting.category_labels IS 'FC-8 추천용 음식 카테고리 선호 (최대 11종, 선택)';
COMMENT ON COLUMN meeting.vibes IS 'FC-8 추천용 분위기 태그 선호 (place.vibe 표준목록, 선택)';
COMMENT ON COLUMN meeting.location_status IS '장소 선정 상태 (BEFORE/RECOMMENDED/VOTING/CONFIRMED)';
```
(`TEXT[]` — place.vibe와 동일 컨벤션)

## 7. V26 (보류 — 지금 파일 생성 안 함)
- U5(V25)까지 끝난 뒤, Build and Test 직전에 생성
- 매핑: `IN_PROGRESS → RECOMMENDED`, `COMPLETED → CONFIRMED`

## 영향 범위 (기존 코드)
- `Meeting.java`, `LocationStatus.java`, `MeetingComputeListStatusTest.java`
- `LocationService.startLocationPhase()` — 가드 호출부만 `assertCanStartLocationPhase()`로 교체 (추천 로직 본문은 U2)
- `GroupService`, `CreateGroupRequest`, `CreateMeetingRequest`, `GroupController`
- `ErrorCode.java` — append만
