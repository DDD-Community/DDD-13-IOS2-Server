# Code Generation Summary — FC-12/13 보완

테스트 결과: **94 tests, 0 failures, 0 errors** (`./gradlew test`)

## 신규 파일
| 파일 | 내용 |
|---|---|
| `src/main/resources/db/migration/V28__place_pick_source_backfill.sql` | pick에 `source` 추가 + `member_id` nullable |
| `meeting/domain/PickSource.java` | enum USER / SYSTEM |
| `meeting/presentation/dto/PlaceTravelBurdenResponse.java` | 친구들 거리보기 응답 |
| `src/test/.../application/PlaceConfirmServiceTest.java` | 동점·rank·수동확정 테스트 |

## 수정 파일
| 파일 | 변경 |
|---|---|
| `meeting/domain/MeetingPlacePick.java` | `source` 필드, `of`(USER)/`ofSystem`(SYSTEM, memberId=null) |
| `meeting/domain/MeetingPlacePickRepository.java` | `saveAll` 추가 |
| `meeting/infrastructure/persistence/MeetingPlacePickJpaEntity.java` | source 매핑, member_id nullable |
| `meeting/infrastructure/persistence/MeetingPlacePickRepositoryImpl.java` | `saveAll` 구현 |
| `global/error/ErrorCode.java` | `PLACE_VOTE_INVALID_CANDIDATE`(MEETING_023) |
| `meeting/application/PlaceVoteService.java` | 백필(단일진입점)·후보=picks·placeId검증·정렬·memberStatuses 전원공개·`getPlaceTravelBurden`·이동부담 대상 picks 기준 |
| `meeting/application/PlaceConfirmService.java` | 공통 비교자(min pickedAt)·1~3위 rank·`confirmByHost` |
| `meeting/presentation/dto/PlaceVoteStatusResponse.java` | `memberStatuses` + `MemberVoteStatus` |
| `meeting/presentation/dto/PlaceResultResponse.java` | `CandidateResult.rank` |
| `meeting/presentation/PlaceVoteController.java` | `POST /place-confirm`, `GET /{placeId}/travel-burden` |
| `src/test/.../application/PlaceVoteServiceTest.java` | 백필·검증·정렬·현황·거리보기 |
| `src/test/.../domain/MeetingPlacePickTest.java` | `ofSystem` 검증 |

## 요구사항 추적
- R1 후보=picks ✅ / R2 백필≥3 ✅ / R3 placeId검증 ✅ / R4 멤버현황 전원공개 ✅
- R5 정렬 ✅ / R8 공통비교자·min(pickedAt) ✅ / R6 수동확정 ✅ / R7 1~3위 rank ✅ / R9 거리보기 ✅

## 마이그레이션 정정
- 설계 문서의 V26 → 실제 **V28** (V26/V27 기존 점유). review/erd 문서 동기화 완료.

## 신규/변경 API
- `POST /api/v1/meetings/{meetingId}/place-confirm` — 호스트 수동 확정
- `GET /api/v1/meetings/{meetingId}/place-vote/{placeId}/travel-burden` — 친구들 거리보기
- `GET .../place-vote` — memberStatuses 전원 제공, 정렬 반영
- `GET .../place-result` — candidates에 rank
