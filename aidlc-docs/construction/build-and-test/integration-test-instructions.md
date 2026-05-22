# Integration Test Instructions — FC-6 모임 리스트

## 목적

실제 PostgreSQL DB와 JWT 인증이 연동된 상태에서 `GET /api/v1/meetings` 엔드포인트를 수동으로 검증한다.

## 사전 조건

```bash
docker-compose up -d   # PostgreSQL 실행
./gradlew bootRun      # Spring Boot 애플리케이션 실행
```

## 테스트 시나리오

### Scenario 1: JWT 없는 요청 → 401

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/meetings
# 기대값: 401
```

### Scenario 2: 유효한 JWT로 조회 → 200 + 데이터

```bash
# 1. 로그인으로 JWT 발급
JWT=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"provider": "KAKAO", "code": "<카카오 인가코드>"}' | jq -r '.accessToken')

# 2. 모임 리스트 조회
curl -s http://localhost:8080/api/v1/meetings \
  -H "Authorization: Bearer $JWT" | jq .
```

**기대값**: 소속 그룹의 모임 카드 배열, IN_PROGRESS 먼저 정렬

### Scenario 3: 그룹 없는 사용자 → 빈 배열

```bash
curl -s http://localhost:8080/api/v1/meetings \
  -H "Authorization: Bearer $JWT_NEW_USER" | jq .
# 기대값: []
```

### Scenario 4: Swagger UI로 시각적 검증

```
http://localhost:8080/swagger-ui/index.html
→ 모임 태그 → GET /api/v1/meetings → Try it out → Authorize
```

## 응답 스키마 검증 체크리스트

- [ ] 응답 배열의 각 항목에 `groupId`, `meetingId` 존재
- [ ] `listStatus` 값이 `IN_PROGRESS | CONFIRMED | CLOSED` 중 하나
- [ ] `locationAddress` 항상 `null` (MVP2 미구현)
- [ ] `members` 배열이 joinedAt 오름차순으로 정렬
- [ ] 탈퇴 회원 `nickname: null`, `profileImageUrl: null`

## Testcontainers 자동화 통합 테스트 (향후)

`org.testcontainers:postgresql` 의존성이 이미 추가되어 있어 Spring Boot 통합 테스트 작성 가능.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MeetingApiIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:15-3.4");
    // ...
}
```
