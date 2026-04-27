# 반가워(Bangawo) 서버

여러 사용자 출발지 기반 중간지점 계산 + 모임 장소 추천 서비스의 백엔드 서버.

## 기술 스택

- Java 17, Spring Boot 3.4.4, Gradle
- Spring Data JPA + PostgreSQL 15 (PostGIS)
- Spring Security + JWT (jjwt 0.12.x)
- Flyway (DB 마이그레이션)
- springdoc-openapi (Swagger UI)

## 로컬 실행 가이드

### 사전 준비

- Java 17+
- Docker Desktop ([설치](https://www.docker.com/products/docker-desktop/))

### 1. 환경변수 설정

프로젝트 루트에 `.env` 파일 생성:

```env
JWT_SECRET_KEY=Base64인코딩된_64바이트_시크릿키

KAKAO_REST_API_KEY=카카오_REST_API_키
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=

DB_HOST=localhost
DB_PORT=5432
DB_NAME=bangawo
DB_USERNAME=bangawo
DB_PASSWORD=bangawo
```

### 2. DB 실행

```bash
docker compose up -d
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 4. 확인

- Swagger UI: http://localhost:8080/swagger-ui.html
- 로그인 테스트: [소셜 로그인 문서](docs/social-login.md) 참고

### 5. 종료

```bash
# 서버: Ctrl+C

# DB 종료 (데이터 유지)
docker compose down

# DB 종료 + 데이터 삭제 (초기화)
docker compose down -v
```

## DB 접속 (DBeaver 등)

| 항목 | 값 |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | bangawo |
| Username | bangawo |
| Password | bangawo |

## 문서

- [소셜 로그인 플로우](docs/social-login.md)
