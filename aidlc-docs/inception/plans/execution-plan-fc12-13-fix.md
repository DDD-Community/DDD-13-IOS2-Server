# Execution Plan — FC-12/13 보완

## 분석 요약
- **Transformation Type**: Single component (meeting 컨텍스트), 일부 place 참조
- **Primary Changes**: 투표 후보 소스 정합화, 백필, 검증, 호스트 현황, 정렬, 수동확정, 1~3위/동점
- **Change Impact**
  - User-facing: Yes (투표 화면 후보·현황·결과 응답 변화)
  - Structural: No (레이어 구조 유지)
  - Data model: 가능성 — 순위 저장/백필 영속화 방식에 따라 Flyway 1건 (AD에서 확정)
  - API: Yes (수동확정 신규, 현황·결과 응답 확장)
  - NFR: 미미 (정합성·결정성)
- **Risk**: Medium (확정 로직 정합성·동점 결정성), Rollback: Easy, Testing: Moderate

## Component Relationships
- **Primary**: `meeting.application` (PlaceVoteService, PlaceConfirmService, PlacePickSchedulerService)
- **Presentation**: PlaceVoteController(+수동확정), PlaceVoteStatusResponse / PlaceResultResponse 확장
- **Domain**: MeetingPlacePick(최초 담은시각 정렬), MeetingConfirmedPlace(순위), Meeting
- **Dependent**: 없음 (외부 호출자 변경 없음, 응답 필드 추가 위주)

## Phases

### 🔵 INCEPTION
- [x] Workspace Detection — COMPLETED
- [x] Reverse Engineering — SKIP (아티팩트 존재)
- [x] Requirements Analysis — COMPLETED (requirements-fc12-13-fix.md)
- [x] User Stories — SKIP (백엔드 API, 역할 2개(호스트/구성원), PRD가 행위 명세 + 버그픽스)
- [x] Workflow Planning — 본 문서
- [ ] **Application Design — EXECUTE** (백필 영속화 방식, 수동확정 서비스, 순위 저장방식, 동점 비교자 공통화 정의)
- [ ] Units Planning — SKIP (단일 응집 단위)
- [ ] Units Generation — SKIP (단일 단위)

### 🟢 CONSTRUCTION
- [ ] Functional Design — EXECUTE (메서드 단위 변경 명세)
- [ ] NFR Requirements/Design — SKIP (기존 충분)
- [ ] Infrastructure Design — SKIP
- [ ] Code Generation — EXECUTE
- [ ] Build and Test — EXECUTE

## Review Artifacts (기존 폴더 갱신)
- `review/fc12/{rules,api,flow,erd}.md` — R1~R5, 수동확정 진입
- `review/fc13/{rules,api,flow,erd}.md` — R6~R8
- `review/overview.md`, `review/project-erd.md` — 스키마 변경 시

## Success Criteria
- 투표/현황/확정이 동일한 후보집합(담김+백필) 사용
- 담기 0개여도 확정까지 무중단
- 동점 처리 결정적, 1~3위 산출
- 단위테스트: 백필(0/1/2/≥3), placeId검증, 호스트현황 분기, 동점 4단계
