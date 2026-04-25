# 반가워(Bangawo) 서버 — 요구사항 문서

> **문서 버전**: 1.0  
> **생성일**: 2026-04-25  
> **상태**: 승인 대기  
> **근거**: project-requirements.md + 명확화 질문 답변

---

## 1. Intent Analysis

| 항목 | 내용 |
|---|---|
| **사용자 요청** | 반가워(Bangawo) 서비스의 백엔드 서버 MVP 1차 개발 |
| **요청 유형** | New Project (Greenfield) |
| **범위** | System-wide — 5개 바운디드 컨텍스트 |
| **복잡도** | Moderate |
| **요구사항 깊이** | Standard |

---

## 2. 기능 요구사항 (Functional Requirements)

### FR-01: 소셜 로그인 (Identity Context)

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-01.1 | 카카오, 네이버, 애플 소셜 로그인 지원 | Must |
| FR-01.2 | 공급자별 토큰 검증 방식 적용 — 카카오/네이버: Access Token → 공급자 API 호출로 사용자 정보 검증, 애플: ID Token(JWT) → 토큰 자체 검증으로 사용자 정보 추출 | Must |
| FR-01.3 | 검증 성공 후 서비스 자체 JWT 발급 (access 1시간 / refresh 30일) | Must |
| FR-01.4 | Refresh Token은 해시 저장 | Must |
| FR-01.5 | 신규 회원 여부를 응답에 플래그로 포함 → iOS가 회원가입 플로우 진입 | Must |
| FR-01.6 | 한 회원 = 한 소셜 계정 (계정 연동 없음) | Must |
| FR-01.7 | 식별자: `social_provider` + `social_user_id` 복합 유니크 (이메일 사용 X) | Must |

### FR-02: 회원가입 / 프로필 (Member Context)

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-02.1 | 닉네임: 2~20자, 중복 허용 | Must |
| FR-02.2 | 금칙어 필터: 직접 작성 정적 리스트 + 정규식, 한글 자모 정규화로 우회 표기 대응 | Must |
| FR-02.3 | 프로필 이미지: nullable 필드로 선언, 실제 저장/업로드 로직은 MVP 후순위 | Should |
| FR-02.4 | 기본 출발지 1개 필수 등록 (회원가입 시) | Must |
| FR-02.5 | 필수 약관 미동의 시 회원가입 거부 | Must |

### FR-03: 출발지 관리 (Member Context)

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-03.1 | 출발지: 라벨, 주소 원문, 위도/경도 저장 | Must |
| FR-03.2 | 기본 출발지는 회원당 정확히 1개 | Must |
| FR-03.3 | 회원당 최대 10개 제한 (설정 가능) | Must |
| FR-03.4 | 좌표는 PostGIS `GEOGRAPHY(POINT, 4326)` 저장 | Must |

### FR-04: 약관 관리 (Terms Context)

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-04.1 | 약관 유형: 이용약관(필수), 개인정보처리방침(필수), 마케팅수신(선택) | Must |
| FR-04.2 | 유형별 버전 관리 | Must |
| FR-04.3 | 약관 재동의: API 응답에 미동의 필수 약관 존재 여부 포함 → iOS가 재동의 화면 표시 | Must |
| FR-04.4 | 동의 이력 DELETE 금지 (법적 증적) | Must |

### FR-05: 디바이스 토큰 (Notification Context)

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-05.1 | iOS 디바이스 토큰 저장/갱신/삭제 | Must |
| FR-05.2 | 푸시 발송 기능은 후순위, 토큰 저장 구조만 MVP 포함 | Should |

### FR-06: API 문서

| ID | 요구사항 | 우선순위 |
|---|---|---|
| FR-06.1 | springdoc-openapi 기반 Swagger UI 제공 | Must |
| FR-06.2 | Swagger UI는 `local`, `dev` 프로필에서만 노출 | Must |

---

## 3. 비기능 요구사항 (Non-Functional Requirements)

### NFR-01: 기술 스택

| 항목 | 결정 |
|---|---|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x |
| 빌드 | Gradle (Wrapper) |
| ORM | Spring Data JPA |
| DB | PostgreSQL 15 + PostGIS |
| 마이그레이션 | Flyway |
| API 문서 | springdoc-openapi (Swagger) |
| 테스트 | JUnit 5 + Testcontainers |
| 로컬 DB | Docker Compose |

### NFR-02: 아키텍처

| 항목 | 결정 |
|---|---|
| 패키지 루트 | `com.bangawo` |
| 아키텍처 패턴 | DDD (Domain-Driven Design) |
| 레이어 구조 | `domain` / `application` / `infrastructure` / `presentation` |
| 모델 분리 | 완전 분리 — Domain Entity + JPA Entity + DTO 별도 클래스, Mapper로 변환 |
| 컨텍스트 분리 | 코드 패키지 레벨 (`com.bangawo.<context>`) |
| 컨텍스트 간 통신 | 도메인 이벤트 / ACL |
| 값 객체 | 불변 |

### NFR-03: 바운디드 컨텍스트

| 컨텍스트 | 책임 | 주요 애그리거트 |
|---|---|---|
| Identity | 소셜 로그인, JWT 발급 | Member, RefreshToken |
| Member | 출발지 관리 | DeparturePlace |
| Terms | 약관 + 동의 이력 | Terms, TermsAgreement |
| Notification | 디바이스 토큰 | DeviceToken |
| Shared Kernel | 공용 값객체 | Coordinate, Address |

### NFR-04: API 규약

| 항목 | 결정 |
|---|---|
| Base URL | `/api/v1/**` |
| 인증 헤더 | `Authorization: Bearer <access_token>` |
| 필드 케이스 | camelCase |
| 날짜 포맷 | ISO-8601 UTC |
| 에러 응답 | 커스텀 포맷: `{ "code": "ERROR_CODE", "message": "설명" }` |
| 페이징 | MVP에서 불필요 (데이터 양 적음) |

### NFR-05: 보안 (Security Baseline 적용)

| 항목 | 결정 |
|---|---|
| HTTPS | 운영 환경 필수 |
| JWT 서명 | HS256, 시크릿은 환경변수 |
| Refresh Token | 해시 저장 |
| 개인정보 로깅 | 금지 |
| Security Baseline | SECURITY-01~15 전체 적용 |

**적용 보안 규칙 요약:**
- SECURITY-01: 저장/전송 시 암호화 (TLS 1.2+, DB 암호화)
- SECURITY-03: 구조화된 애플리케이션 로깅 (민감 정보 제외)
- SECURITY-05: 모든 API 파라미터 입력 검증 (타입, 길이, 포맷, 인젝션 방지)
- SECURITY-08: 애플리케이션 레벨 접근 제어 (인증 필수, IDOR 방지, CORS 제한)
- SECURITY-09: 보안 강화 (기본 자격증명 제거, 스택 트레이스 노출 금지)
- SECURITY-10: 공급망 보안 (의존성 버전 고정, 취약점 스캔)
- SECURITY-11: 보안 설계 원칙 (관심사 분리, 심층 방어, Rate Limiting)
- SECURITY-12: 인증/자격증명 관리 (세션 관리, 브루트포스 방지, 하드코딩 금지)
- SECURITY-15: 예외 처리 (fail-closed, 리소스 정리, 글로벌 에러 핸들러)

> **참고**: SECURITY-02(네트워크 중간자 로깅), SECURITY-04(HTTP 보안 헤더), SECURITY-06(최소 권한 IAM), SECURITY-07(네트워크 설정), SECURITY-13(무결성 검증), SECURITY-14(알림/모니터링)은 인프라/배포 단계에서 적용 평가.

### NFR-06: 데이터베이스

| 항목 | 결정 |
|---|---|
| 명명 규칙 | snake_case |
| PK | `id BIGINT IDENTITY` |
| FK | `<referenced>_id` |
| Boolean | `is_*` |
| 타임스탬프 | `created_at`, `updated_at`, `deleted_at`(nullable) |
| 좌표 | PostGIS `GEOGRAPHY(POINT, 4326)` + `latitude`/`longitude` 병행 |
| 마이그레이션 | Flyway, 초기 스크립트에 PostGIS 확장 + 약관 시드 데이터 포함 |

### NFR-07: 인프라 (참고)

| 항목 | 결정 |
|---|---|
| 배포 대상 | Oracle Cloud Always Free (권장) |
| 구성 | Docker로 앱 + PostgreSQL(PostGIS) 구동 |
| 비용 | 0원 영구 (ARM 4코어 24GB) |

---

## 4. 도메인 모델 개요

### 4.1 ERD 관계

```
member (1) ── (N) departure_place
member (1) ── (N) refresh_token
member (1) ── (N) terms_agreement (N:1) terms
member (1) ── (N) device_token
```

### 4.2 핵심 테이블

| 테이블 | 설명 | 주요 제약 |
|---|---|---|
| `member` | 소셜 로그인 + 프로필 통합 | `(social_provider, social_user_id)` UNIQUE |
| `refresh_token` | JWT Refresh Token 해시 저장 | `member_id` FK |
| `departure_place` | 출발지 (라벨, 주소, 좌표) | `is_default=true` 회원당 1개, 최대 10개 |
| `terms` | 약관 (유형별 버전 관리) | `(type, version)` UNIQUE |
| `terms_agreement` | 동의 이력 (DELETE 금지) | `(member_id, terms_id)` UNIQUE |
| `device_token` | iOS 디바이스 토큰 | `(member_id, token)` UNIQUE |

---

## 5. 결정 사항 요약

| # | 항목 | 결정 | 결정 방식 |
|---|---|---|---|
| D-01 | 패키지명 | `com.bangawo` | 사용자 선택 |
| D-02 | 소셜 로그인 토큰 검증 | 공급자별 방식 (카카오/네이버: Access Token, 애플: ID Token) | AI 추천 → 사용자 위임 |
| D-03 | 에러 응답 포맷 | 커스텀 `{ "code", "message" }` | 사용자 선택 |
| D-04 | 페이징 | MVP 불필요 | 사용자 선택 |
| D-05 | 도메인/영속 모델 분리 | 완전 분리 (Domain + JPA Entity + DTO) | 사용자 선택 |
| D-06 | 금칙어 사전 | 직접 작성 정적 리스트 + 정규식 | 사용자 선택 |
| D-07 | 프로필 이미지 | nullable 필드, 저장 로직 후순위 | 사용자 선택 |
| D-08 | 로컬 DB | Docker Compose | 사용자 선택 |
| D-09 | 약관 재동의 | API 응답에 미동의 약관 정보 포함 | 사용자 선택 |
| D-10 | API 문서 | Swagger UI만 | 사용자 선택 |
| D-11 | TDD Extension | 비활성화 | 사용자 선택 |
| D-12 | PBT Extension | 비활성화 | 사용자 선택 |
| D-13 | Security Baseline | 활성화 (전체 규칙 적용) | 사용자 선택 |

---

## 6. MVP 범위 외 (후순위)

| 항목 | 비고 |
|---|---|
| 모임방 생성 및 출발 위치 수집 | MVP 2차 |
| 중간지점 계산 및 장소 추천 | MVP 2차 |
| 회원 탈퇴 플로우 | MVP 2차 |
| 프로필 이미지 업로드/변경 | MVP 2차 |
| 푸시 알림 발송 | 토큰 저장만 MVP 1차 |
| 모임장 전용 기능 | MVP 2차 (모임방) |

---

## 7. 열린 이슈

| # | 이슈 | 상태 |
|---|---|---|
| O1 | 프로젝트명 확정 | 미결 (가칭 Bangawo 사용 중) |
| O2 | 클라우드 최종 선택 | Oracle Cloud Always Free 권장, 미확정 |
| O3 | 중간지점 계산 방식 | MVP 2차 |
| O4 | 장소 추천 데이터 소스 | MVP 2차 |
| O5 | 회원 탈퇴 플로우 | MVP 2차 |
| O6 | 프로필 이미지 업로드 방식 | MVP 2차 |
| O7 | 푸시 채널 (APNs vs FCM) | 발송 구현 시 결정 |
| O8 | 도메인/영속 모델 분리 | **해결 — 완전 분리** |
| O9 | 금칙어 사전 소스 | **해결 — 직접 작성 정적 리스트** |
