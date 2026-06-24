# Code Generation Plan — FC-12/13 보완 (단일 유닛)

> 이 문서가 코드 생성의 단일 소스. 단계 완료마다 [x] 즉시 갱신.
> Brownfield: 기존 파일은 **in-place 수정**, 신규만 생성. 중복 파일(`*_new`, `*_modified`) 금지.
> 워크스페이스 루트: `src/main/java`, `src/test/java`, `src/main/resources/db/migration`

## 요구사항 추적 (R = requirements-fc12-13-fix.md)
- R1 후보=담긴장소 / R2 백필≥3 / R3 placeId검증 / R4 멤버현황 전원공개
- R5 정렬 / R5(R8) 공통비교자·min(pickedAt) / R6 수동확정 / R7 1~3위 rank
- R9 친구들 거리보기(단일 장소 이동부담)

---

## Step 1: DB 마이그레이션 (V28)
- [x] **생성** `src/main/resources/db/migration/V28__place_pick_source_backfill.sql`
  - `ALTER TABLE meeting_place_pick ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'USER';`
  - `ALTER TABLE meeting_place_pick ALTER COLUMN member_id DROP NOT NULL;`
  - COMMENT 추가
- 근거: R2 / domain-entities.md (V26→V28 정정)

## Step 2: 도메인 — PickSource + MeetingPlacePick
- [x] **생성** `meeting/domain/PickSource.java` (enum USER, SYSTEM)
- [x] **수정** `meeting/domain/MeetingPlacePick.java`
  - `PickSource source` 필드 추가
  - `of(...)` → source=USER 세팅
  - `ofSystem(meetingId, placeId)` 팩토리 추가 (memberId=null, source=SYSTEM)
- 근거: R2

## Step 3: 도메인 Repository 인터페이스 확장
- [x] **수정** `meeting/domain/MeetingPlacePickRepository.java`
  - `List<MeetingPlacePick> saveAll(List<MeetingPlacePick> picks)` 추가
- 근거: R2 (백필 배치 저장; 단건 save 반복도 가능하나 명시)

## Step 4: 인프라 — JPA Entity / Repository 반영
- [x] **수정** `meeting/infrastructure/persistence/MeetingPlacePickJpaEntity.java`
  - `source` 컬럼 매핑(@Enumerated(STRING) 또는 String), `member_id` nullable
  - `from()`/`toDomain()` source 반영
- [x] **수정** `MeetingPlacePickJpaRepository.java` (필요 시 saveAll은 기본 제공)
- [x] **수정** `MeetingPlacePickRepositoryImpl.java`
  - `saveAll` 구현
- 근거: R2

## Step 5: ErrorCode 추가
- [x] **수정** `global/error/ErrorCode.java`
  - `PLACE_VOTE_INVALID_CANDIDATE(BAD_REQUEST, "MEETING_023", "후보에 없는 장소에 투표할 수 없습니다")`
- 근거: R3

## Step 6: 비즈니스 로직 — PlaceVoteService (핵심)
- [x] **수정** `meeting/application/PlaceVoteService.java`
  - `createSession` 진입부에 `backfillCandidatesIfNeeded(meetingId)` 호출 (백필 단일 진입점)
  - **신규 private** `backfillCandidatesIfNeeded`: picks<3 → 추천 rank순 `ofSystem` 적재
  - `startVoting`: 담기 0개 가드 제거 (LOCATION_NOT_RECOMMENDED throw 삭제)
  - `submitVote`: 후보 소스 = picks(distinct), maxVotes=max(1, n/2), **placeId 소속검증** 추가
  - `getVoteStatus`: 후보 소스 = picks, 정렬(투표전 가나다/투표후 득표순), **memberStatuses 전원 제공**
  - `computeAndSaveTravelBurdens`: 대상 placeId = picks distinct (recMap으로 nearestStationId 매핑)
  - **신규** `getPlaceTravelBurden(meetingId, placeId, memberId)` — 친구들 거리보기 (R9)
  - 의존성 추가: `MemberRepository`(auth.domain) 주입
- 근거: R1·R2·R3·R4·R5·R9

## Step 7: 비즈니스 로직 — PlaceConfirmService (공통 비교자 + rank + 수동확정)
- [x] **수정** `meeting/application/PlaceConfirmService.java`
  - **신규 private** `buildCandidateComparator(picks, voteCount, secondsSum, transfersSum)`
    - 득표↓ → 시간합↑ → 환승합↑ → min(pickedAt)↑
  - `confirmPlace`: 공통 비교자 사용 (pickOrder long → min pickedAt 으로 교체)
  - `getResult`: 공통 비교자로 정렬 후 상위 3개 rank(1·2·3), 나머지 rank=0
  - **신규** `confirmByHost(meetingId, memberId)`: 호스트·VOTING 검증 → toConfirmed → confirmPlace
  - 의존성 추가: `GroupMemberRepository` 주입(호스트 검증)
- 근거: R5(R8)·R6·R7

## Step 8: 응답 DTO 수정/신규
- [x] **수정** `presentation/dto/PlaceVoteStatusResponse.java`
  - `List<MemberVoteStatus> memberStatuses` 필드 + nested `MemberVoteStatus(Long memberId, String name, boolean completed)`
- [x] **수정** `presentation/dto/PlaceResultResponse.java`
  - `CandidateResult`에 `int rank` 추가 (첫 필드)
- [x] **생성** `presentation/dto/PlaceTravelBurdenResponse.java`
  - `(PlaceSummary place, List<MemberBurden> burdens)` + `MemberBurden(Long memberId, String name, int seconds, int transfers, boolean isLongest)`
- 근거: R4·R7·R9

## Step 9: API 레이어 — PlaceVoteController
- [x] **수정** `meeting/presentation/PlaceVoteController.java`
  - **신규** `POST /{meetingId}/place-confirm` → `placeConfirmService.confirmByHost(...)`
  - **신규** `GET /{meetingId}/place-vote/{placeId}/travel-burden` → `placeVoteService.getPlaceTravelBurden(...)`
  - @Operation Swagger 설명 추가
- 근거: R6·R9

## Step 10: 단위 테스트
- [x] **수정/추가** `src/test/java/.../application/PlaceVoteServiceTest.java`
  - 백필 0/1/2/≥3개, placeId 검증(PLACE_VOTE_INVALID_CANDIDATE), 정렬(투표전/후), memberStatuses 전원 제공, getPlaceTravelBurden
- [x] **추가** `src/test/java/.../application/PlaceConfirmServiceTest.java` (없으면 신규)
  - 동점 4단계(min pickedAt), 1~3위 rank, 전원기권, confirmByHost(호스트/VOTING 분기)
- [x] **수정** `src/test/java/.../domain/MeetingPlacePickTest.java`
  - `ofSystem` 팩토리(source=SYSTEM, memberId=null)
- 근거: requirements 테스트 항목

## Step 11: 리뷰 산출물 동기화 (코드 ↔ 문서)
- [x] **수정** `aidlc-docs/construction/review/fc12/erd.md` — V26 표기 → **V28** 정정
- [x] **수정** `aidlc-docs/construction/review/fc13/erd.md` — V26 표기 → **V28** 정정
- [x] **수정** `aidlc-docs/construction/review/fc12/api.md` (Step 13.5에서 최종 반영)
- [x] **수정** `aidlc-docs/construction/review/project-erd.md` — pick.source/nullable 반영

## Step 12: 코드 요약 문서
- [x] **생성** `aidlc-docs/construction/internal/fc12-13-fix/code/summary.md` (수정/생성 파일 목록)

---

## 영향 파일 요약
| 구분 | 파일 |
|---|---|
| 신규 | V28 sql, PickSource, PlaceTravelBurdenResponse, (PlaceConfirmServiceTest) |
| 수정 | MeetingPlacePick(+Jpa/Impl), MeetingPlacePickRepository, ErrorCode, PlaceVoteService, PlaceConfirmService, PlaceVoteStatusResponse, PlaceResultResponse, PlaceVoteController, 테스트들, review 문서 |
| 불변 | Meeting, 세션/투표/이동부담 도메인, 추천 도메인, 스케줄러(호출부 그대로) |

## 검증 (Build & Test에서)
- `./gradlew test` 통과, Flyway V28 적용 확인
