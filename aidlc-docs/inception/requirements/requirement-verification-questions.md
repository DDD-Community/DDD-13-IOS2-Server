# Requirements Verification Questions

아래 질문에 답변해 주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택지 알파벳을 기입하시면 됩니다.
"Other"를 선택한 경우 `[Answer]:` 뒤에 설명을 추가해 주세요.

---

## Question 1
프로젝트 루트 패키지명을 어떻게 할까요? (예: `com.bangawo`)

A) `com.bangawo` (가칭 그대로 사용)
B) 다른 패키지명 사용
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2
소셜 로그인 시 iOS가 전달하는 "공급자 토큰"은 어떤 것인가요?

A) OAuth Access Token — 서버가 각 공급자 API를 호출하여 사용자 정보를 검증
B) OIDC ID Token (JWT) — 서버가 토큰 자체를 검증하여 사용자 정보 추출
C) 공급자별로 다름 (카카오/네이버는 Access Token, 애플은 ID Token)
X) Other (please describe after [Answer]: tag below)

[Answer]: 뭔말인지 모르겠어 나는 그냥 JWT로 인증하려했는데

## Question 3
에러 응답 포맷을 어떻게 할까요?

A) RFC 7807 Problem Details (`application/problem+json`) 표준 사용
B) 커스텀 에러 포맷 (`{ "code": "...", "message": "..." }`)
C) Spring Boot 기본 에러 포맷 사용
X) Other (please describe after [Answer]: tag below)

[Answer]: IOS에서 보려면 B가 좋으려나? 잘몰라서 되물어볼꼐

## Question 4
페이징이 필요한 API가 있나요? (예: 출발지 목록, 약관 목록 등)

A) 페이징 불필요 — MVP 범위에서 데이터 양이 적어 전체 조회로 충분
B) 커서 기반 페이징 적용
C) 오프셋 기반 페이징 적용
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 5
열린 이슈 O8: 도메인 모델과 영속 모델 분리 수준은?

A) 완전 분리 — 도메인 엔티티와 JPA 엔티티를 별도 클래스로 유지, 매퍼로 변환
B) 부분 분리 — JPA 엔티티에 도메인 로직 포함하되, DTO는 별도 분리
C) 통합 — JPA 엔티티가 곧 도메인 모델 (실용적 접근)
X) Other (please describe after [Answer]: tag below)

[Answer]: 이것도 무슨 말인지 모르겠네 근데 그건있어 나중에 모임장만 할 수 있는 기능이 있어

## Question 6
열린 이슈 O9: 금칙어 사전 소스는?

A) 직접 작성한 정적 리스트 + 정규식 (MVP에서는 소규모 목록으로 시작)
B) 외부 오픈소스 금칙어 사전 활용 (예: korean-bad-words 등)
C) MVP에서는 금칙어 필터 구현 생략, 후순위로 이동
X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 7
회원가입 시 프로필 이미지 "저장만"이라고 했는데, 구체적으로 어떤 형태인가요?

A) iOS가 소셜 공급자에서 받은 프로필 이미지 URL을 서버에 전달 → 서버는 URL만 저장
B) iOS가 이미지 파일을 서버에 업로드 → 서버가 저장소에 저장 후 URL 기록
C) MVP에서는 프로필 이미지 필드 자체를 nullable로 두고 실제 저장 로직은 후순위
X) Other (please describe after [Answer]: tag below)

[Answer]: C

## Question 8
로컬 개발 환경에서 PostgreSQL + PostGIS는 어떻게 구동할 예정인가요?

A) Docker Compose로 PostgreSQL + PostGIS 컨테이너 구동
B) 로컬에 직접 PostgreSQL + PostGIS 설치
C) Testcontainers만 사용 (테스트 시에만 DB 구동)
X) Other (please describe after [Answer]: tag below)

[Answer]: 일단 A가 제일 좋겠지?

## Question 9
약관 재동의 플로우에서, 새 버전 약관이 등록되면 기존 사용자에게 어떻게 알릴 건가요?

A) 로그인/API 호출 시 서버가 미동의 필수 약관 존재 여부를 응답에 포함 → iOS가 재동의 화면 표시
B) 푸시 알림으로 재동의 요청 (MVP 후순위 - 푸시 발송 기능 없음)
C) MVP에서는 재동의 플로우 구현 생략, 약관 버전 관리 구조만 준비
X) Other (please describe after [Answer]: tag below)

[Answer]: 아 이것도 뭔말인지 모르겠네 설명좀해줘봐

## Question 10
Swagger UI 외에 API 문서 관련 추가 요구사항이 있나요?

A) Swagger UI만으로 충분
B) Swagger UI + API 문서 자동 export (예: OpenAPI JSON/YAML)
X) Other (please describe after [Answer]: tag below)

[Answer]: A 근데 B에 API 문서 자동은 뭐야?

---

## Extension Opt-In Questions

아래는 프로젝트에 적용할 수 있는 확장 규칙입니다. 활성화 여부를 선택해 주세요.

## Question 11: TDD Code Generation Extension
코드 생성 시 Test-Driven Development (TDD) 방식을 사용할까요?

A) Yes — TDD 워크플로우로 코드 생성 (복잡한 비즈니스 로직, 데이터 변환, 장기 유지보수가 필요한 프로젝트에 권장; ~1.5-2x 토큰 비용이지만 기능 누락 방지 및 거의 무결점 결과물 생산)
B) No — 표준 코드 생성 워크플로우 사용 (단순 프로토타입, 일회성 스크립트, 비즈니스 로직이 적은 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: 일단 B로 하고 나중에 필요하면 A로 변경하자

## Question 12: Property-Based Testing Extension
Property-Based Testing (PBT) 규칙을 이 프로젝트에 적용할까요?

A) Yes — 모든 PBT 규칙을 blocking 제약으로 적용 (비즈니스 로직, 데이터 변환, 직렬화, 상태 관리 컴포넌트가 있는 프로젝트에 권장)
B) Partial — 순수 함수와 직렬화 round-trip에만 PBT 규칙 적용 (알고리즘 복잡도가 제한적인 프로젝트에 적합)
C) No — 모든 PBT 규칙 건너뛰기 (단순 CRUD, UI 전용, 비즈니스 로직이 없는 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]: PBT가 뭔지 모른다..

## Question 13: Security Baseline Extension
보안 확장 규칙을 이 프로젝트에 적용할까요?

A) Yes — 모든 보안 규칙을 blocking 제약으로 적용 (프로덕션 수준 애플리케이션에 권장)
B) No — 모든 보안 규칙 건너뛰기 (PoC, 프로토타입, 실험적 프로젝트에 적합)
X) Other (please describe after [Answer]: tag below)

[Answer]:  보안 확장 규칙이 뭐야 ...
