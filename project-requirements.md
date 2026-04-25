# 반가워(Bangawo) 서버 요구사항 정의서

> **상태**: v0.2 초안 (MVP 1차)
> **범위**: 백엔드 서버. iOS는 별도.

---

## 1. 서비스 개요

여러 사용자 출발지 기반으로 공정한 중간지점을 계산하고 주변 모임 장소를 추천하는 서비스. iOS 앱 클라이언트.

프로젝트명 "Bangawo"는 가칭.

### MVP 1차 범위
1. 소셜 로그인 (카카오 / 네이버 / 애플) + JWT
2. 회원가입 (닉네임, 프로필 이미지, 기본 출발지)
3. 출발지 다중 라벨링 관리
4. 약관 동의 이력 관리
5. 푸시 알림용 디바이스 토큰 저장
6. Swagger 문서 제공

### MVP 후순위 (본 문서 범위 외)
- 모임방 생성 및 출발 위치 수집
- 중간지점 계산 및 장소 추천
- 회원 탈퇴 플로우
- 프로필 이미지 직접 업로드 방식

---

## 2. 기술 스택 & 아키텍처

- Java 17, Spring Boot 3.x, Gradle (Wrapper)
- Spring Data JPA + PostgreSQL 15 (PostGIS)
- JWT
- springdoc-openapi (Swagger)
- Flyway
- JUnit 5 + Testcontainers

### DDD 원칙
- 레이어: `domain / application / infrastructure / presentation`
- 애그리거트 루트를 통해서만 상태 변경
- 컨텍스트 간 통신은 도메인 이벤트 / ACL
- 값 객체는 불변

### 바운디드 컨텍스트

| 컨텍스트 | 책임 | 주요 애그리거트 |
|---|---|---|
| Identity | 소셜 로그인, JWT | Member (소셜 정보 + 프로필 통합), RefreshToken |
| Member | 출발지 관리 | DeparturePlace |
| Terms | 약관 + 동의 이력 | Terms, TermsAgreement |
| Notification | 디바이스 토큰 | DeviceToken |
| Shared Kernel | 공용 값객체 | Coordinate, Address |

> **참고**: 컨텍스트 분리는 **코드 패키지(`com.bangawo.<context>`) 레벨**에서 하고, DB 테이블은 평탄하게 둔다. 계정 연동이 없어 Identity와 Member의 핵심 엔티티는 `member` 하나로 통합.

---

## 3. 기능 요구사항

### 3.1 소셜 로그인 (Identity)
- 카카오, 네이버, 애플 지원
- iOS가 공급자 토큰 전달 → 서버 검증 → 신규/기존 판별 → JWT 발급
- 신규 계정이면 응답에 플래그 → iOS가 회원가입 스텝 진입
- JWT 정책: **access 1시간 / refresh 30일**, refresh는 해시 저장
- 한 회원은 **하나의 소셜 계정만** 사용 (계정 연동 기능 없음)
- 공급자 + 공급자 사용자 ID 복합 유니크. 이메일 식별자 사용 X (애플 비공개 가능)

### 3.2 회원가입 / 프로필 (Member)
- 닉네임: 2~20자, **중복 허용**(모임방 내에서만 구분), 변경주기 제한 없음
- 금칙어 필터: 정적 리스트 + 정규식, 한글 자모 정규화로 일부 우회 표기 대응
- 프로필 이미지: 회원가입 시 저장만. 업로드/변경 방식은 후순위에서 결정
- 기본 출발지 1개 필수 등록
- 회원가입 시 필수 약관 미동의면 거부

### 3.3 출발지 관리 (Member)
- 라벨(집/회사 등), 주소 원문, 위도/경도 저장
- 기본 출발지는 회원당 정확히 1개
- 회원당 최대 개수 제한 (기본 10, 조정 가능)
- 좌표는 PostGIS `GEOGRAPHY(POINT, 4326)` 저장

### 3.4 약관 (Terms)
- 유형: 이용약관(필수), 개인정보처리방침(필수), 마케팅수신(선택) — 유형별 버전 관리
- 약관 개정 시 재동의 플로우 지원
- **동의 이력은 DELETE 금지** (법적 증적)

### 3.5 디바이스 토큰 (Notification)
- iOS 디바이스 토큰 저장/갱신/삭제
- 발송 기능은 후순위. 토큰 저장 구조만 MVP 포함
- 푸시 채널(APNs 직결 vs FCM 경유)은 발송 구현 시점에 결정

---

## 4. 공통 규약

- Base URL: `/api/v1/**`
- 인증 헤더: `Authorization: Bearer <access_token>`
- 필드 케이스: camelCase
- 날짜: ISO-8601 UTC
- Swagger UI는 `local`, `dev` 프로필에서만 노출

---

## 5. DB 설계 (참고용)

> ⚠️ **본 섹션은 참고용이다.** 실제 스키마는 RE/RA 단계 및 구현 시점에 확정한다. 테이블 목록과 관계는 큰 그림 합의를 위한 초안 수준.

### 명명 규칙
- 테이블: snake_case, 의미 있는 엔티티명 그대로 사용 (컨텍스트 prefix는 이름이 겹칠 때만)
- PK: `id BIGINT IDENTITY`
- FK: `<referenced>_id`
- Boolean: `is_*`
- 타임스탬프: `created_at`, `updated_at`, `deleted_at`(nullable)
- 좌표: PostGIS `GEOGRAPHY(POINT, 4326)` 컬럼 `geo_point` + `latitude`, `longitude` 병행

### ERD

```
member (1) ── (N) departure_place
member (1) ── (N) refresh_token
member (1) ── (N) terms_agreement (N:1) terms
member (1) ── (N) device_token
```

### 테이블 스펙

#### member
소셜 로그인 정보와 프로필을 통합한 회원 테이블.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| social_provider | VARCHAR(20) | KAKAO / NAVER / APPLE |
| social_user_id | VARCHAR(255) | 공급자 고유 ID |
| email | VARCHAR(255) NULL | |
| nickname | VARCHAR(20) | 중복 허용 |
| profile_image_url | VARCHAR(500) NULL | |
| status | VARCHAR(20) | ACTIVE / SUSPENDED / WITHDRAWN |
| created_at, updated_at, deleted_at | TIMESTAMPTZ | |

- 유니크: `(social_provider, social_user_id)`

#### refresh_token
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| member_id | BIGINT FK | |
| token_hash | VARCHAR(255) | 해시 저장 |
| expires_at | TIMESTAMPTZ | |
| revoked_at | TIMESTAMPTZ NULL | |
| created_at | TIMESTAMPTZ | |

#### departure_place
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| member_id | BIGINT FK | |
| label | VARCHAR(10) | 예: "집" |
| address | VARCHAR(255) | |
| latitude, longitude | DOUBLE PRECISION | |
| geo_point | GEOGRAPHY(POINT, 4326) | |
| is_default | BOOLEAN | |
| created_at, updated_at | TIMESTAMPTZ | |

- 부분 유니크: `is_default=true`는 회원당 1개만

#### terms
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| type | VARCHAR(30) | TERMS_OF_SERVICE / PRIVACY_POLICY / MARKETING |
| version | VARCHAR(20) | |
| title | VARCHAR(200) | |
| content | TEXT | |
| is_required | BOOLEAN | |
| effective_from | TIMESTAMPTZ | |
| created_at | TIMESTAMPTZ | |

- 유니크: `(type, version)`

#### terms_agreement
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| member_id | BIGINT FK | |
| terms_id | BIGINT FK | |
| agreed_at | TIMESTAMPTZ | |

- 유니크: `(member_id, terms_id)`
- **DELETE 금지** (법적 증적)

#### device_token
| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| member_id | BIGINT FK | |
| token | VARCHAR(500) | |
| platform | VARCHAR(10) | IOS |
| app_version | VARCHAR(20) NULL | |
| created_at, updated_at | TIMESTAMPTZ | |

- 유니크: `(member_id, token)`

### 마이그레이션
- Flyway. 최초 스크립트에 `CREATE EXTENSION IF NOT EXISTS postgis;` 포함.
- 초기 약관 시드 데이터 포함.

---

## 6. 보안

- HTTPS 필수 (운영)
- JWT 서명: HS256, 시크릿은 환경변수
- refresh token은 해시 저장
- 개인정보 로깅 금지

---

## 7. 인프라 (결정 필요)

사용자 제약: AWS/GCP 프리티어 소진, 저비용 지향.

후보:
- **Oracle Cloud Always Free**: 0원 영구, ARM 4코어 24GB
- 네이버클라우드 신규 크레딧: 0원 기한부
- 저가 VPS (Contabo 등): $4~5/월

권장: Oracle Cloud Always Free에 Docker로 앱 + PostgreSQL(PostGIS) 구동.

---

## 8. 열린 이슈

| # | 이슈 |
|---|---|
| O1 | 프로젝트명 확정 |
| O2 | 클라우드 최종 선택 |
| O3 | 중간지점 계산 방식 |
| O4 | 장소 추천 데이터 소스 |
| O5 | 회원 탈퇴 플로우 |
| O6 | 프로필 이미지 업로드 방식 |
| O7 | 푸시 채널 (APNs vs FCM) |
| O8 | 도메인 모델과 영속 모델 분리 수준 |
| O9 | 금칙어 사전 소스 |

---

## 9. 변경 이력

| 버전 | 날짜 | 내용 |
|---|---|---|
| v0.1 | 2026-04-24 | 초안 |
| v0.2 | 2026-04-24 | 세부 API/페이징/에러포맷/엔드포인트 상세 제거. 토큰 유효기간 상향. 프로필 이미지는 MVP에서 저장만, 업로드 방식은 후순위로 이동. |
| v0.3 | 2026-04-24 | 한 회원=한 소셜 정책 확정 → `user_account`/`member` 통합. 테이블명 겹침 제거(`member`, `terms` 등). JWT access 1h / refresh 30d로 조정. DB 설계는 참고용임을 명시. |
