---
description: AI-DLC 워크플로우 시작 또는 재개 (Inception → Construction → Operations)
allowed-tools: Bash, Read, Write, Edit, WebSearch, WebFetch
argument-hint: [구현할 기능 설명 (선택)]
---

AI-DLC(AI-Driven Development Life Cycle) 워크플로우를 실행한다.

**사용자 요청**: $ARGUMENTS

---

## 규칙 파일 경로

모든 단계 실행 전 해당 단계의 규칙 파일을 반드시 읽는다.

```
.aidlc-rule-details/          ← 규칙 상세 루트 (Claude Code 환경)
  common/
  inception/
  construction/
  operations/
  extensions/
```

---

## STEP 0: 세션 판단

`aidlc-docs/aidlc-state.md` 존재 여부를 Bash로 확인한다.

### 기존 세션 발견 시
1. `.aidlc-rule-details/common/session-continuity.md` 읽기
2. `aidlc-docs/aidlc-state.md`에서 현재 단계·상태 파싱
3. Welcome Back 메시지 표시 (session-continuity.md 템플릿 사용, aidlc-state.md 값으로 채움)
4. 사용자에게 A) 이어서 진행 / B) 처음부터 시작 선택지 제공
5. 응답 대기

### 신규 세션 시
1. `.aidlc-rule-details/common/welcome-message.md` 전체 내용을 **수정 없이** 그대로 출력
2. 내부 참조용으로 로드 (출력 안 함):
   - `.aidlc-rule-details/common/process-overview.md`
   - `.aidlc-rule-details/common/content-validation.md`
   - `.aidlc-rule-details/common/question-format-guide.md`
3. Extensions 스캔: `.aidlc-rule-details/extensions/` 하위 `*.opt-in.md` 파일만 로드 (full rule 파일은 로드하지 않음)
4. STEP 1로 자동 진행

---

## INCEPTION PHASE

### STEP 1: Workspace Detection (항상 실행)

1. `.aidlc-rule-details/inception/workspace-detection.md` 읽기
2. 파일에 정의된 모든 단계 순서대로 실행
3. `aidlc-docs/` 디렉토리 생성 후 `aidlc-docs/aidlc-state.md` 생성 (workspace-detection.md Step 4 포맷)
4. `aidlc-docs/audit.md` 생성 및 초기 로그 기록
5. 완료 메시지 표시 (workspace-detection.md Step 5 포맷)
6. **승인 불필요** — 자동으로 다음 단계 진행

---

### STEP 2: Reverse Engineering (브라운필드 + 아티팩트 없을 때만)

실행 조건:
- 기존 코드베이스 감지됨
- `aidlc-docs/inception/reverse-engineering/` 아티팩트 없음 (또는 stale)

1. `.aidlc-rule-details/inception/reverse-engineering.md` 읽기
2. 파일에 정의된 모든 단계 실행
3. **사용자 명시적 승인 대기** — 승인 전 다음 단계 금지
4. audit.md에 응답 로그 추가 (append만)

---

### STEP 3: Requirements Analysis (항상 실행)

1. `.aidlc-rule-details/inception/requirements-analysis.md` 읽기
2. 브라운필드면 리버스 엔지니어링 아티팩트 로드
3. 사용자 요청($ARGUMENTS) 및 `docs/prd/` 등 기존 PRD 문서 참조
4. Extensions opt-in 프롬프트를 이 단계에서 사용자에게 제시
5. 필요 시 질문 파일 생성 (question-format-guide.md 형식 준수 — 채팅 인라인 질문 금지)
6. **사용자 명시적 승인 대기**
7. audit.md 로그 추가

---

### STEP 4: User Stories (조건부)

실행 조건: 신규 사용자 기능, 여러 사용자 유형, 복잡한 비즈니스 요구사항

1. `.aidlc-rule-details/inception/user-stories.md` 읽기
2. Part 1 (계획): 스토리 계획 + 질문 파일 생성 → 승인
3. Part 2 (생성): 승인된 계획으로 스토리·페르소나 생성
4. **사용자 명시적 승인 대기**
5. audit.md 로그 추가

---

### STEP 5: Workflow Planning (항상 실행)

1. `.aidlc-rule-details/inception/workflow-planning.md` 읽기
2. 모든 이전 아티팩트 로드 (RE + RA + Stories)
3. 실행할 단계·깊이·유닛 분해 계획 수립
4. Mermaid 다이어그램 포함 시 content-validation.md 규칙으로 검증 후 작성
5. **사용자 명시적 승인 대기** (사용자가 단계 포함/제외 요청 가능함을 명시)
6. audit.md 로그 추가

---

### STEP 6: Application Design (조건부)

실행 조건: 신규 컴포넌트/서비스 필요, 서비스 레이어 설계 필요

1. `.aidlc-rule-details/inception/application-design.md` 읽기
2. 리버스 엔지니어링 아티팩트 로드 (브라운필드)
3. **사용자 명시적 승인 대기**
4. audit.md 로그 추가

---

### STEP 7: Units Generation (조건부)

실행 조건: 복수의 유닛으로 분해 필요, 복잡한 시스템

1. `.aidlc-rule-details/inception/units-generation.md` 읽기
2. **사용자 명시적 승인 대기**
3. audit.md 로그 추가

---

## CONSTRUCTION PHASE

### 유닛별 루프 (각 유닛을 완전히 마친 후 다음 유닛으로)

각 유닛에 대해 아래 단계를 순서대로 실행한다:

---

#### Functional Design (조건부, 유닛별)

실행 조건: 신규 데이터 모델, 복잡한 비즈니스 로직, 비즈니스 규칙 상세 설계 필요

1. `.aidlc-rule-details/construction/functional-design.md` 읽기
2. 해당 유닛에 대해 실행
3. **표준 2-옵션 완료 메시지** (파일 정의 형식 그대로 — 3-옵션 등 변형 금지)
4. **사용자 승인 후 다음 단계**

---

#### NFR Requirements (조건부, 유닛별)

실행 조건: 성능/보안/확장성 고려, 기술 스택 선택 필요

1. `.aidlc-rule-details/construction/nfr-requirements.md` 읽기
2. **표준 2-옵션 완료 메시지**
3. **사용자 승인 후 다음 단계**

---

#### NFR Design (조건부, 유닛별)

실행 조건: NFR Requirements 단계가 실행된 경우

1. `.aidlc-rule-details/construction/nfr-design.md` 읽기
2. **표준 2-옵션 완료 메시지**
3. **사용자 승인 후 다음 단계**

---

#### Infrastructure Design (조건부, 유닛별)

실행 조건: 인프라 서비스 매핑, 클라우드 리소스 명세 필요

1. `.aidlc-rule-details/construction/infrastructure-design.md` 읽기
2. **표준 2-옵션 완료 메시지**
3. **사용자 승인 후 다음 단계**

---

#### Code Generation (항상 실행, 유닛별)

1. `.aidlc-rule-details/construction/code-generation.md` 읽기
2. **Part 1 (계획)**: 체크박스 포함 상세 계획 생성 → 사용자 승인 대기
3. **Part 2 (생성)**: 승인된 계획대로 코드·테스트·아티팩트 생성
4. **표준 2-옵션 완료 메시지**
5. **사용자 승인 후** 다음 유닛 루프 또는 Build and Test

---

### Build and Test (항상 실행)

1. `.aidlc-rule-details/construction/build-and-test.md` 읽기
2. `aidlc-docs/construction/build-and-test/` 에 파일 생성:
   - `build-instructions.md`
   - `unit-test-instructions.md`
   - `integration-test-instructions.md`
   - `performance-test-instructions.md` (해당 시)
   - `build-and-test-summary.md`
3. **사용자 승인 후 Operations**

---

## OPERATIONS PHASE

1. `.aidlc-rule-details/operations/operations.md` 읽기
2. 현재 플레이스홀더 — 향후 확장 예정

---

## 절대 규칙 (위반 시 즉시 중단)

| # | 규칙 |
|---|---|
| 1 | 사용자 명시적 승인 없이 단계 건너뛰기 금지 |
| 2 | `audit.md`는 항상 **append(추가)**만 — 전체 덮어쓰기 절대 금지 |
| 3 | 체크박스는 작업 완료와 **같은 인터랙션**에서 즉시 업데이트 |
| 4 | 코드 파일은 워크스페이스 루트에만 — `aidlc-docs/` 안에 코드 생성 금지 |
| 5 | Construction 단계는 **표준 2-옵션 완료 메시지만** — 창의적 변형 금지 |
| 6 | 질문은 반드시 별도 `.md` 파일로 — 채팅 인라인 질문 금지 |
| 7 | 파일 생성 전 content-validation.md 규칙으로 Mermaid/ASCII 검증 |
| 8 | 모든 사용자 입력을 audit.md에 **원문 그대로** 기록 (요약·가공 금지) |
