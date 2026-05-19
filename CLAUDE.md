# Bangawo 프로젝트 개발 가이드

## 프로젝트 개요
- Spring Boot 3.4.4 / Java 17
- DDD(Domain-Driven Design) 적용 — 학습 겸 실전 적용
- PostgreSQL + JPA + Flyway

---

## 패키지 구조

```
com.bangawo
├── {context}/               # 바운디드 컨텍스트 단위 (auth, member, group, meeting ...)
│   ├── domain/              # 도메인 모델, 리포지토리 인터페이스, 도메인 서비스
│   ├── application/         # 유스케이스, 애플리케이션 서비스
│   ├── presentation/        # Controller, Request/Response DTO
│   └── infrastructure/      # JpaEntity, JpaRepository, RepositoryImpl, 외부 클라이언트
└── global/                  # 공통 설정, 보안, 예외 처리
```

---

## DDD 핵심 원칙

### 레이어 의존 방향
```
presentation → application → domain ← infrastructure
```
- `domain`은 외부(JPA, Spring)에 의존하지 않는다
- `infrastructure`가 `domain`의 Repository 인터페이스를 구현한다
- `presentation`은 `domain` 객체를 직접 노출하지 않는다 (DTO 변환 필수)

### 도메인 모델
- 비즈니스 로직은 도메인 객체 안에 둔다 (빈약한 도메인 모델 지양)
- JPA Entity(`*JpaEntity`)와 도메인 모델(`*`)을 분리한다
- 도메인 이벤트가 필요한 경우 도메인 레이어에서 발행한다

### 리포지토리
- 인터페이스는 `domain` 패키지에 위치
- 구현체(`*RepositoryImpl`)는 `infrastructure` 패키지에 위치
- 리포지토리 인터페이스는 도메인 모델을 반환한다 (JpaEntity 반환 금지)

### 애플리케이션 서비스
- 트랜잭션 경계를 담당한다
- 도메인 객체를 조합하는 오케스트레이션 역할
- 비즈니스 로직을 직접 구현하지 않는다 (도메인에 위임)

---

## 코드 스타일

- 간결하고 가독성 좋은 코드를 지향한다
- 불필요한 주석을 달지 않는다 — 코드가 의도를 설명해야 한다
- 메서드는 하나의 책임만 가진다
- 매직 넘버/문자열은 상수 또는 enum으로 분리한다
- 디자인 패턴은 과도하게 적용하지 않고 필요한 곳에만 사용한다

---

## 예외 처리

- 비즈니스 예외는 `BusinessException(ErrorCode)` 형태로 던진다
- `ErrorCode`에 HTTP 상태코드와 메시지를 함께 정의한다
- `GlobalExceptionHandler`에서 일괄 처리한다

---

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 도메인 모델 | 단순 명사 | `Member`, `Group` |
| JPA Entity | 도메인명 + `JpaEntity` | `MemberJpaEntity` |
| Repository 인터페이스 | 도메인명 + `Repository` | `MemberRepository` |
| Repository 구현체 | 도메인명 + `RepositoryImpl` | `MemberRepositoryImpl` |
| Application Service | 도메인명 + `Service` | `MemberService` |
| DTO | 용도 명시 | `MemberResponse`, `LoginRequest` |

- 테이블명 prefix 중복 금지 (`member_member_id` → `member_id`)
- 컨텍스트 경계는 YAGNI 원칙으로 — 필요할 때 분리한다

---

## Flyway 마이그레이션

- 스키마 변경은 반드시 Flyway 스크립트로 관리한다
- 파일명: `V{버전}__{설명}.sql` (예: `V2__add_group_table.sql`)
- 한 번 적용된 스크립트는 수정하지 않는다

---

## 브랜치 전략

- `main`: 배포 브랜치
- `dev`: 통합 브랜치
- `feature/{기능명}`: 기능 개발 → dev PR
