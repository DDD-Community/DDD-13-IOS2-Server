# Build and Test Summary

> 최신 갱신: 2026-06-24 — FC-12/13 보완 사이클(mvp3-1 갭)

## Build Result
| 항목 | 결과 |
|---|---|
| **Build Tool** | Gradle 8.12 (Wrapper) |
| **Command** | `./gradlew clean build` |
| **Status** | BUILD SUCCESSFUL |
| **Artifacts** | `build/libs/bangawo-0.0.1-SNAPSHOT.jar`, `*-plain.jar` |
| **실행일** | 2026-06-24 |

## Test Result
| 항목 | 결과 |
|---|---|
| **총 테스트 수** | 94 |
| **성공** | 94 |
| **실패** | 0 |
| **에러** | 0 |
| **스킵** | 0 |

> 이전 사이클 77 → 이번 보완으로 +17 (백필·placeId검증·정렬·전원현황·동점4단계·rank·수동확정·거리보기)

## 이번 사이클 변경 (FC-12/13 보완)

### 투표 (FC-12)
- 후보 소스 = 담긴 장소(`meeting_place_pick`, USER+SYSTEM) — 추천15개에서 정합화
- 백필 단일 진입점 `PlaceVoteService.createSession → backfillCandidatesIfNeeded` (담긴<3 시 추천 rank순 SYSTEM 적재)
- `submitVote` placeId 후보소속 검증(`PLACE_VOTE_INVALID_CANDIDATE`)
- `getVoteStatus` 정렬(미투표 가나다 / 투표 후 득표순) + `memberStatuses` **전원 공개**
- `getPlaceTravelBurden` 신규 — 친구들 거리보기(단일 장소 이동부담)

### 확정 (FC-13)
- 공통 비교자 `buildCandidateComparator` (득표↓ → 시간합↑ → 환승합↑ → **min(pickedAt)**↑)
  - `confirmPlace`(자동/마감/수동) · `getResult`(1~3위) 공유
- `getResult` 1~3위 rank 부여 (후보<3이면 후보 수만큼, 4위↓ rank=0)
- `confirmByHost` 신규 — 호스트 수동 확정(VOTING 한정)
- 마감 자동 확정 스케줄러(`PlaceVoteSchedulerService.closeExpiredSessions`)는 기존 유지 + 개선된 공통 비교자 자동 반영

## Flyway 마이그레이션 (이번 추가)
| 버전 | 내용 |
|---|---|
| **V28** | `meeting_place_pick` `source`(USER/SYSTEM) 컬럼 추가 + `member_id` nullable |

> 설계 문서상 V26 가정 → 실제 V26/V27 점유로 **V28**로 확정.

## Overall Status
- **Build**: Success
- **All Tests**: Pass (94/94)
- **Ready for Operations**: Yes
