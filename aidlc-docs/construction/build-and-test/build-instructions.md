# Build Instructions

## Prerequisites
- **Build Tool**: Gradle 8.12 (Wrapper 포함)
- **Java**: OpenJDK 17
- **DB**: PostgreSQL 15 (로컬 실행 불필요 — 테스트는 Mock 기반)

## Build Steps

### 1. Compile
```bash
./gradlew compileJava
```

### 2. Full Build (컴파일 + 테스트 + JAR)
```bash
./gradlew clean build --no-daemon
```

### 3. 실행 가능 JAR 생성
```bash
./gradlew bootJar
# 결과: build/libs/bangawo-server-*.jar
```

## Expected Output
```
BUILD SUCCESSFUL
```

## Troubleshooting
- **NPE in test**: Mock 누락 확인 — @InjectMocks 대상 클래스의 새 의존성이 @Mock 선언되었는지 확인
- **Flyway 오류**: V{n} 파일 체크섬 충돌 → 적용된 마이그레이션 수정 금지
