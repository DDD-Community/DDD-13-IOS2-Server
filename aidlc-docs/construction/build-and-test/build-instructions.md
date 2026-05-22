# Build Instructions — Bangawo Server

## Prerequisites

- **Build Tool**: Gradle (Wrapper 포함 — `./gradlew` 사용)
- **Java**: 17 (JDK 17+)
- **Database**: PostgreSQL 15 (로컬 실행 시 Docker Compose 필요)
- **Environment Variables**: `.env` 파일 (spring-dotenv 로드)

## 환경 변수 설정

```env
# .env (프로젝트 루트)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bangawo
DB_USER=bangawo
DB_PASSWORD=bangawo
JWT_SECRET=<base64-64바이트>
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000
```

## Build Steps

### 1. 로컬 DB 실행 (최초 또는 재시작 시)

```bash
docker-compose up -d
```

### 2. 컴파일만 검증

```bash
./gradlew compileJava
```

**성공 기준**: `BUILD SUCCESSFUL`

### 3. 전체 빌드 (테스트 제외)

```bash
./gradlew build -x test
```

**생성 아티팩트**: `build/libs/bangawo-0.0.1-SNAPSHOT.jar`

### 4. 전체 빌드 (테스트 포함)

```bash
./gradlew build
```

## 성공 기준

- `BUILD SUCCESSFUL`
- 컴파일 에러 0개
- 허용 경고: Lombok unchecked 경고 (제네릭 파라미터 raw type 사용 시)

## Troubleshooting

### `Could not resolve` 의존성 에러
- 원인: Maven Central 접근 불가
- 해결: 네트워크 확인 후 `./gradlew build --refresh-dependencies`

### `Flyway migration failed`
- 원인: DB 미실행 또는 마이그레이션 스크립트 충돌
- 해결: `docker-compose up -d` 후 `docker-compose logs db` 확인
