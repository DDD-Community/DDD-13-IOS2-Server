# Execution Plan — Bangawo MVP1

## Analysis Summary

### Transformation Scope
- **Type**: Multi-context feature addition (Brownfield)
- **Primary Changes**: group, meeting 두 개 신규 바운디드 컨텍스트 추가
- **Related Components**: global (ErrorCode, SecurityConfig 확장), auth (Member 참조)

### Change Impact Assessment
- **User-facing**: Yes — 그룹 생성/초대/모임/투표 전체 신규 API
- **Structural**: Yes — group, meeting 패키지 신규 생성
- **Data Model**: Yes — V7+ Flyway 마이그레이션 필요 (group, meeting, membership, date_vote 등)
- **API**: Yes — /api/v1/groups, /api/v1/meetings 신규 엔드포인트
- **NFR**: Yes — SSE 스트리밍, @Scheduled, FCM, Security Baseline 인가 규칙

### Risk Assessment
- **Risk Level**: Medium-High
- **이유**: SSE 구현, 투표 상태머신, 생명주기 스케줄러, FCM 연동 등 복잡도 있음
- **Rollback**: 신규 컨텍스트라 기존 코드 영향 최소 — 롤백 상대적으로 용이

---

## Workflow Visualization

```
INCEPTION PHASE
+----------------------------------+
| [완료] Workspace Detection       |
| [완료] Reverse Engineering       |
| [완료] Requirements Analysis     |
| [SKIP] User Stories              |
| [진행] Workflow Planning         |
| [실행] Application Design        |
| [실행] Units Generation          |
+----------------------------------+
              |
              v
CONSTRUCTION PHASE (유닛별 반복)
+----------------------------------+
| [실행] Functional Design         |
| [실행] NFR Requirements          |
| [실행] NFR Design                |
| [SKIP] Infrastructure Design     |
| [실행] Code Generation           |
| [실행] Build and Test            |
+----------------------------------+
```

---

## 실행 단계 목록

### INCEPTION PHASE
- [x] Workspace Detection — COMPLETED
- [x] Reverse Engineering — COMPLETED
- [x] Requirements Analysis — COMPLETED
- [x] User Stories — SKIP (PRD가 이미 상세 플로우 포함, 두 역할 정의됨)
- [x] Workflow Planning — IN PROGRESS
- [ ] Application Design — EXECUTE (신규 컴포넌트/서비스 설계 필요)
- [ ] Units Generation — EXECUTE (3개 유닛으로 분해)

### CONSTRUCTION PHASE (유닛별)
- [ ] Functional Design — EXECUTE (투표 상태머신, 도메인 모델 상세 설계)
- [ ] NFR Requirements — EXECUTE (SSE, Scheduler, Security Baseline, FCM)
- [ ] NFR Design — EXECUTE (NFR 패턴 적용 설계)
- [ ] Infrastructure Design — SKIP (클라우드 인프라 변경 없음)
- [ ] Code Generation — EXECUTE (항상)
- [ ] Build and Test — EXECUTE (항상)

### OPERATIONS PHASE
- [ ] Operations — PLACEHOLDER

---

## 유닛 분해 — FC 순서 기준 (협업 역할 분담용)

| 유닛 | FC | 내용 | 핵심 복잡도 |
|---|---|---|---|
| **Unit 1** | FC-4 | 그룹 & 첫 모임 생성 (그룹 생성 시 모임 동시 생성, 테마 태그) | 그룹/모임 동시 트랜잭션 |
| **Unit 2** | FC-5 | 구성원 초대 & 합류 (초대 코드 발급/검증, 멤버십 등록) | 초대 코드 만료 처리 |
| **Unit 3** | FC-6 | 모임 리스트 — 홈 화면 (상태별 정렬, 본인 모임만 조회) | 상태 계산 로직 |
| **Unit 4** | FC-7 | 모임 상세 + 날짜 투표 A/B + SSE 실시간 현황 + 자동 종료 스케줄러 | SSE, 투표 상태머신, @Scheduled |
| **Unit 5** | FC-7-1 | 내 정보 수정 (참석여부, 출발지, 수정 잠금) | 날짜 투표 종료 후 잠금 |
| **Unit 6** | FC-8 | 그룹 생명주기 (그룹 종료, 새 모임 시작, 호스트 탈퇴/위임) | 호스트 랜덤 배정 |
| **Unit 7** | 공통 | FCM 푸시 알림 (투표 시작/마감/확정/초대) | FCM 인터페이스 설계 |

---

## 의존성 & 병렬 작업 가능 범위

```
Unit 1 (FC-4: 그룹 생성) ← 모든 유닛의 기반, 선행 필수
    ├── Unit 2 (FC-5: 초대)   ─┐
    ├── Unit 3 (FC-6: 리스트)  ├── Unit 1 완료 후 병렬 가능
    ├── Unit 5 (FC-7-1: 내정보)─┘
    ├── Unit 4 (FC-7: 투표/SSE) ← Unit 1 완료 후
    └── Unit 6 (FC-8: 생명주기) ← Unit 1 완료 후
Unit 7 (Notification) ← Unit 4, 6 완료 후 (이벤트 트리거 확정 필요)
```

---

## 별도 태스크 (코드와 독립)

| 태스크 | 담당 | 내용 |
|---|---|---|
| Firebase 설정 | iOS 팀 협업 | 서비스 계정 키 발급, APNs 인증서 등록 |
| **GCP 배포** | 백엔드 | GCP MCP로 Cloud Run / GKE 서버 배포 (MVP1 코드 완성 후) |

---

## 성공 기준
- group, meeting 바운디드 컨텍스트 DDD 패턴 준수
- 모든 API Security Baseline 인가 규칙 적용 (호스트/구성원 분리)
- SSE 날짜 투표 현황 정상 동작
- @Scheduled 모임 자동 종료 동작
- FCM 코드 구현 완료 (Firebase 키 없이도 인터페이스 완성)
- V7+ Flyway 마이그레이션 스크립트 완성
- GCP 서버 배포 완료 (Operations 단계)
