# AI-DLC 개발 워크플로우 가이드

이 프로젝트는 **AI-DLC(AI-Driven Development Life Cycle)** 방식으로 기능을 개발합니다.  
Claude Code에 내장된 `/aidlc` 스킬을 통해 요구사항 분석부터 코드 생성까지 단계별로 진행하며, 그 결과물이 `aidlc-docs/`에 쌓입니다.

---

## 전체 흐름

```
INCEPTION PHASE          CONSTRUCTION PHASE (유닛별 반복)
─────────────────        ──────────────────────────────────────
1. Workspace Detection   1. Functional Design   → 도메인 설계
2. Reverse Engineering   2. NFR Requirements    → 성능/보안 요구사항
3. Requirements Analysis 3. NFR Design          → NFR 구현 설계
4. Workflow Planning     4. Code Generation     → 실제 코드 작성
5. Application Design    5. Build and Test      → 빌드/테스트 지침
6. Units Generation
```

- **Inception**: 프로젝트/기능 전체를 분석해 무엇을 어떤 순서로 만들지 계획합니다.
- **Construction**: 유닛(기능 단위) 하나씩 설계 → 코드 생성 루프를 돌립니다.
- 각 단계는 AI가 결과물을 제시하고, **사람이 승인해야** 다음 단계로 넘어갑니다.

---

## 산출물 구조

```
aidlc-docs/
├── aidlc-state.md              ← 현재 진행 상태 (AI가 여기서 이어서 시작)
├── audit.md                    ← 전체 진행 이력 로그
├── inception/                  ← Inception 단계 산출물
│   ├── plans/execution-plan.md ← 전체 유닛 분해 및 실행 계획
│   └── requirements/           ← 요구사항 분석 결과
└── construction/
    ├── internal/               ← AI 작업용 (계획, 상세 설계 초안)
    │   ├── plans/              ← 각 유닛별 코드 생성 계획
    │   └── unit-N-fcX/        ← 유닛별 설계 상세
    └── review/                 ← 사람이 읽는 검토 산출물 ★
        ├── project-erd.md      ← 전체 테이블 마스터 ERD
        └── fcX/
            ├── erd.md          ← 해당 기능 시점 전체 ERD + 컬럼 설명
            ├── rules.md        ← 비즈니스 규칙 (사람 언어로)
            └── api.md          ← API 명세 (요청/응답/에러)
```

> `review/` 폴더가 핵심입니다. 개발 전 `erd.md`와 `rules.md`로 설계를 확인하고, 개발 후 `api.md`로 API를 검토합니다.

---

## 시작하는 방법

### 새 기능 개발 시작

프로젝트 루트에서 Claude Code를 열고:

```
/aidlc [기능 설명 또는 요청 내용]
```

예시:
```
/aidlc FC-5 구성원 초대 & 합류 기능 개발해줘.
aidlc-docs/aidlc-state.md 보고 이어서 진행해.
```

---

### 이어서 진행할 때 (세션이 끊긴 경우)

Claude Code는 `aidlc-docs/aidlc-state.md`를 읽어 자동으로 어디서 멈췄는지 파악합니다.

```
/aidlc aidlc-state.md 보고 [다음 단계]부터 이어서 진행해줘.
```

현재 이 프로젝트의 진행 상태는 `aidlc-docs/aidlc-state.md`에서 확인할 수 있습니다.

---

## 단계별 설명

### Inception Phase

프로젝트 또는 신규 기능을 처음 분석할 때 한 번 실행합니다.  
이미 완료된 경우 다시 실행할 필요 없습니다.

| 단계 | 설명 | 조건 |
|---|---|---|
| Workspace Detection | 기존 코드 구조 파악 | 항상 |
| Reverse Engineering | 기존 DDD 패턴 분석 | 브라운필드 프로젝트 |
| Requirements Analysis | PRD 분석, 기능 목록 확정 | 항상 |
| Workflow Planning | 유닛 분해, 실행 순서 계획 | 항상 |
| Application Design | 서비스/컴포넌트 설계 | 신규 컴포넌트 필요 시 |
| Units Generation | 유닛별 상세 명세 | 복수 유닛 분해 필요 시 |

이 프로젝트의 유닛 분해 결과: `aidlc-docs/inception/plans/execution-plan.md`

---

### Construction Phase — 유닛 하나씩 반복

각 유닛(기능 단위)을 아래 순서대로 완전히 마친 후 다음 유닛으로 넘어갑니다.

#### 1단계: Functional Design
도메인 모델, ERD, 비즈니스 규칙을 설계합니다.

결과물:
- `aidlc-docs/construction/review/fcX/erd.md` — Mermaid ERD + 컬럼 코멘트
- `aidlc-docs/construction/review/fcX/rules.md` — 비즈니스 규칙
- `aidlc-docs/construction/review/project-erd.md` — 마스터 ERD (누적)

**이 단계에서 ERD를 직접 수정하면 코드 생성에 반영됩니다.**  
테이블명/컬럼명을 바꾸고 싶으면 `review/fcX/erd.md`를 고치면 됩니다.

---

#### 2단계: NFR Requirements / NFR Design (조건부)
성능, 보안, SSE 등 비기능 요구사항이 있을 때만 실행합니다.

---

#### 3단계: Code Generation
승인된 설계대로 실제 Java 코드를 생성합니다.

DDD 레이어 순서로 생성됩니다:
```
도메인 모델 → 리포지토리 인터페이스 → 애플리케이션 서비스
→ JPA 엔티티 → 리포지토리 구현체 → 컨트롤러/DTO
→ Flyway 마이그레이션
```

완료 후 결과물: `aidlc-docs/construction/review/fcX/api.md`

**코드 생성 전에 AI는 반드시 `review/fcX/erd.md`를 먼저 읽습니다.**  
ERD를 수정했다면 코드에 자동 반영됩니다.

---

#### 4단계: Build and Test
빌드 명령어, 테스트 실행 방법이 `aidlc-docs/construction/build-and-test/`에 생성됩니다.

---

## 리뷰 방법

코드 생성이 완료되면 `aidlc-docs/construction/review/fcX/` 폴더를 열어봅니다.

| 파일 | 확인할 내용 |
|---|---|
| `erd.md` | 테이블 구조가 의도대로인가? 컬럼명, 관계, 제약조건 |
| `rules.md` | 비즈니스 규칙이 빠진 게 없나? 예외 처리 케이스 |
| `api.md` | 엔드포인트 경로, 요청/응답 형식, 에러 코드 |

수정이 필요하면 해당 파일을 직접 고친 뒤 Claude Code에 재생성을 요청하면 됩니다.

---

## 현재 이 프로젝트의 진행 현황

| 유닛 | 기능 | 상태 |
|---|---|---|
| Unit 1 | FC-4 그룹 & 첫 모임 생성 | ✅ 완료 |
| Unit 2 | FC-5 구성원 초대 & 합류 | 대기 |
| Unit 3 | FC-6 모임 리스트 (홈 화면) | 대기 |
| Unit 4 | FC-7 날짜 투표 + SSE + 스케줄러 | 대기 |
| Unit 5 | FC-7-1 내 정보 수정 | 대기 |
| Unit 6 | FC-8 그룹 생명주기 | 대기 |
| Unit 7 | FCM 푸시 알림 | 대기 |

전체 계획: `aidlc-docs/inception/plans/execution-plan.md`

---

## 주의사항

- `aidlc-docs/` 안에는 코드를 두지 않습니다. 문서만 있습니다.
- 실제 코드는 항상 `src/main/java/` 아래에 생성됩니다.
- 한 번 적용된 Flyway 스크립트(`V숫자__설명.sql`)는 절대 수정하지 않습니다.
- 각 단계는 사람이 승인해야 다음 단계로 넘어갑니다. AI 혼자 끝까지 달리지 않습니다.
