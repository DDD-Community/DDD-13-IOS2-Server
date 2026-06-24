# Application Design — FC-12/13 보완

## 1. 핵심 설계 결정

### D-A. 백필 영속화 = `meeting_place_pick` 재사용 + `source` 구분
- 담긴 후보 3개 미만 시 추천(rank↑, 미담김 제외)으로 채운 장소를 **같은 테이블에 INSERT**.
- **스키마 변경 (Flyway 신규, V26 가정)**:
  - `ALTER TABLE meeting_place_pick ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'USER';`
  - `ALTER TABLE meeting_place_pick ALTER COLUMN member_id DROP NOT NULL;` (SYSTEM 행은 member_id NULL)
- 백필 행: `source='SYSTEM'`, `member_id=NULL`, `picked_at=now()`.
- **집계 분리**:
  - 후보 집합(투표/확정 대상) = 전체 행 distinct place_id (USER+SYSTEM)
  - 담기 현황·함께담기 N·완료 구성원 = `source='USER'` 만

### D-B. 순위(1~3위) = 조회 시 계산 (테이블 추가 없음)
- 1위는 기존대로 `meeting_confirmed_place`에 저장.
- 1~3위 rank는 `getResult`에서 **확정 로직과 동일 비교자**로 정렬 후 rank 부여(결정적). 후보<3이면 후보 수만큼.
- 별도 rank 테이블 미도입(MVP 단순화).

### D-C. 동점 비교자 공통화
- `confirmPlace`(1위 선정)와 `getResult`(1~3위)·향후 수동확정이 **단일 Comparator** 사용:
  1. 득표수 desc → 2. 이동시간합 asc → 3. 환승합 asc → 4. **최초 담은 시각(min picked_at) asc**
- 4번 위해 후보별 `min(picked_at)` 맵 산출(USER/SYSTEM 무관, 담긴/백필 시각 기준).

## 2. 컴포넌트 변경

### PlaceVoteService
- `startVoting` / 자동전환 경로: 후보<3 시 **백필 호출** 추가 후 세션 생성.
- `submitVote`: 후보 소스 = pick(전체), `maxVotes=max(1, 후보수/2)`, **placeId ⊆ 후보 검증** 추가.
- `getVoteStatus`: 후보 소스 = pick(전체). 정렬(투표 전 가나다순/후 득표순). 호스트면 **구성원별 완료여부 리스트** 포함.

### PlaceConfirmService
- `confirmPlace`: 공통 Comparator 사용(4번 시각 기준 수정). 후보 소스 = pick.
- `getResult`: 1~3위 rank 산출·정렬, 응답에 rank 추가.
- **신규** `confirmByHost(meetingId, memberId)`: 호스트·VOTING 검증 → toConfirmed → confirmPlace.

### PlacePickSchedulerService
- 마감 자동전환 시에도 **백필 후** 세션 생성(0개 멈춤 제거).

### MeetingPlacePick (domain)
- `ofSystem(meetingId, placeId)` 팩토리(member_id null, source SYSTEM).
- `source` 필드 추가.

### Presentation
- `PlaceVoteController`: **POST `/{meetingId}/place-confirm`** (수동 확정) 추가.
- `PlaceVoteStatusResponse`: 정렬 반영 + 호스트용 `memberStatuses[{memberId, name, completed}]`(nullable).
- `PlaceResultResponse.CandidateResult`: `rank` 필드 추가.

## 3. 백필 로직 (의사코드)
```
picks = pickRepo.findByMeetingId(meetingId)            // USER+SYSTEM
distinctPlaceIds = picks.placeId.distinct()
if distinctPlaceIds.size < 3:
    recs = recRepo.findByMeetingIdOrderByRank()        // rank asc
    for r in recs:
        if r.placeId not in distinctPlaceIds:
            pickRepo.save(MeetingPlacePick.ofSystem(meetingId, r.placeId))
            distinctPlaceIds.add(r.placeId)
        if distinctPlaceIds.size >= 3: break
// 추천도 부족하면 가능한 만큼만
```

## 4. 영향 없는 것
- 외부 호출자 계약: 응답 필드 **추가만**(기존 필드 유지) → 하위호환.
- subway_edge/그래프, 추천 로직, FC-8/9 담기 토글 동작 불변(단 담기 토글은 source='USER' 기준 그대로).

## 5. 마이그레이션 요약
- **V26**: meeting_place_pick `source` 컬럼 + member_id nullable.
- 그 외 테이블 변경 없음.
