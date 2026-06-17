# AI-DLC State Tracking

## Project Information
- **Project**: Bangawo (반가워) — 모임 조율 서비스 백엔드
- **Project Type**: Brownfield (MVP1 + MVP2(FC-4~7) 완료, 신규 MVP2+3(FC-8~13) 추가)
- **Current Feature**: 장소 선정 ~ 확정 플로우 (FC-8 ~ FC-13)
- **Feature Branch**: feature/place-selection-flow
- **Session Start**: 2026-06-16T00:00:00+09:00
- **Current Stage**: INCEPTION 완료 → CONSTRUCTION READY

## Workspace State
- **Existing Code**: Yes (Java 171 files, Spring Boot/DDD, 17 Flyway migrations)
- **Existing Contexts**: global, auth, member, group, meeting, subway, storage
- **Reverse Engineering Needed**: Yes (stale — 139→171 files, 10→17 migrations, 신규 subway/storage 컨텍스트 + place/subway_edge)
- **Workspace Root**: /c/dev/tmp/ddd/Server

## Code Location Rules
- **Application Code**: Workspace root (NEVER in aidlc-docs/)
- **Documentation**: aidlc-docs/ only
- **Structure patterns**: See code-generation.md Critical Rules

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Security baseline | Yes | Requirements Analysis |
| Property-Based Testing | Partial | Requirements Analysis |
| TDD code generation | No | Requirements Analysis |

## GCP 배포 현황
- **Cloud Run URL**: https://bangawo-server-gzfcbbuf4q-du.a.run.app
- **Cloud SQL**: PostgreSQL 15, bangawo_prod / bangawo_user
- **배포 방식**: main 브랜치 머지 → GitHub Actions 자동 배포

## Feature Scope (FC-8 ~ FC-13)
- **PRD**: docs/prd/mvp3.md
- FC-8: 중간 지역 산출 + 장소 추천 (상위 15개, 역 귀속 태깅) — 일부 선행(midpoint V13) 존재
- FC-9: 장소 후보 리스트 탐색 및 담기 (담기/취소, 담기 완료 정의)
- FC-11: 투표 생성 및 마감일 설정 (+1/+3/+7 프리셋)
- FC-12: 장소 투표 진행 (익명 다중, 이동 부담 = subway_edge 최단경로)
- FC-13: 장소 자동 확정 (4단계 순위 로직)
- 알림: 상태 전환 스케줄러 기반 푸시/인앱

## 기존 선행 자산 (재사용)
- meeting_participant (V11), subway_station (V12/V16), midpoint_station_candidate (V13)
- place (V16), **subway_edge (V17)** — 지하철 이동그래프, 이동시간·환승 계산용 (사용자가 직접 추가)

## Stage Progress

### INCEPTION PHASE (FC-8~13)
- [x] Workspace Detection — Brownfield, 171 Java files, 17 Flyway migrations, RE stale 판정
- [x] Reverse Engineering — architecture/code-structure/api-doc/timestamp 갱신
- [x] Requirements Analysis — requirements.md 확정 (2축 유지, PRD 플로우, 거리 용도분리)
- [x] User Stories — SKIP (백엔드 API, 역할 2개 단순, PRD가 행위 상세 명세)
- [x] Workflow Planning — execution-plan.md (AD·UG·FD·NFR EXECUTE / US·Infra SKIP)
- [x] Application Design — components/methods/services/dependency/통합 5종
- [x] Units Generation — unit-of-work 3종 (5유닛, FC 매핑)
- [x] Review Artifacts — fc8·fc9·fc11·fc12·fc13 각4 + fc-group-lifecycle 보관 + fc4 수정 + overview/project-erd 갱신

### CONSTRUCTION PHASE
- [x] U1 기반 — LocationStatus 4-state, Meeting 가드, categoryLabels/vibes 입력확장, ErrorCode 8건, V18 (완료, 승인됨)
- [x] U2 추천 (FC-8) — 완료
- [x] U3 담기 (FC-9) — 완료
- [x] U4 그래프+투표 (FC-11/12)
- [x] U5 확정 (FC-13)
- [x] Build and Test — 77 tests, 0 failures
- current_unit: COMPLETE

## Previous Cycle (완료 — FC-4~7 MVP2 subway/midpoint)
- INCEPTION + CONSTRUCTION 완료 (meeting_participant, subway context, midpoint 계산 API)
- 산출물: aidlc-docs/inception/, aidlc-docs/construction/ (이전 사이클)

## Phase
- phase: OPERATIONS
- stage: READY
- status: AWAITING_START
- last_updated: 2026-06-17T02:00:00+09:00
