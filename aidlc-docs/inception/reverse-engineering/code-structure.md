# Code Structure

## Build System
- **Type**: Gradle
- **Java**: 17
- **Spring Boot**: 3.4.4

## DDD 구현 패턴 (MVP1 구현 시 반드시 동일 패턴 적용)

### 패턴 1: 도메인 모델
```java
// 위치: {context}/domain/
// 규칙: JPA 애노테이션 없음, @Getter + @Builder, 비즈니스 메서드 포함
@Getter
public class Member {
    private Long id;
    // ... fields

    @Builder
    public Member(...) { ... }

    // 정적 팩토리 메서드
    public static Member create(...) { ... }

    // 비즈니스 메서드
    public void updateProfile(...) { ... }
}
```

### 패턴 2: JPA 엔티티 (도메인과 분리)
```java
// 위치: {context}/infrastructure/persistence/
// 규칙: *JpaEntity 접미사, from(domain) + toDomain() 변환 메서드
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {
    // JPA 필드들

    public static MemberJpaEntity from(Member domain) { ... }
    public Member toDomain() { ... }
}
```

### 패턴 3: Repository 인터페이스 (도메인 레이어)
```java
// 위치: {context}/domain/
// 규칙: *Repository 접미사, 도메인 모델 반환 (JpaEntity 반환 금지)
public interface MemberRepository {
    Optional<Member> findById(Long id);
    Member save(Member member);
}
```

### 패턴 4: Repository 구현체 (인프라 레이어)
```java
// 위치: {context}/infrastructure/persistence/
// 규칙: *RepositoryImpl 접미사, JpaRepository 위임, toDomain() 변환
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository jpaRepository;

    @Override
    public Member save(Member member) {
        return jpaRepository.save(MemberJpaEntity.from(member)).toDomain();
    }
}
```

### 패턴 5: 애플리케이션 서비스
```java
// 위치: {context}/application/
// 규칙: *Service 접미사, @Transactional, 오케스트레이션만, 비즈니스 로직 금지
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    // 생성자 주입만 사용
}
```

### 패턴 6: 컨트롤러
```java
// 위치: {context}/presentation/
// 규칙: @RestController, @RequestMapping("/api/v1/..."), DTO 사용
// DTO 위치: {context}/presentation/dto/
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController { ... }
```

### 패턴 7: 예외 처리
```java
// 사용법: throw new BusinessException(ErrorCode.XXX)
// ErrorCode: HTTP 상태코드 + 코드문자열 + 메시지 정의
// GlobalExceptionHandler가 일괄 처리
throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
```

### 패턴 8: DB 마이그레이션
```sql
-- 파일명: V{n}__{설명}.sql
-- BIGINT GENERATED ALWAYS AS IDENTITY (시퀀스 방식)
-- 테이블명 prefix 중복 금지 (member_id O, member_member_id X)
CREATE TABLE group_info (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ...
);
```

## 기존 파일 인벤토리

### global
- `global/config/SecurityConfig.java` — JWT 필터 체인, 공개 엔드포인트 설정
- `global/config/WebConfig.java` — CORS 설정
- `global/config/SwaggerConfig.java` — OpenAPI 설정
- `global/security/JwtProvider.java` — Access/Refresh 토큰 생성·검증
- `global/security/JwtAuthenticationFilter.java` — 요청별 JWT 파싱·인증 주입
- `global/error/ErrorCode.java` — 비즈니스 에러 코드 목록
- `global/error/BusinessException.java` — 비즈니스 예외
- `global/error/GlobalExceptionHandler.java` — 전역 예외 처리
- `global/common/Coordinate.java` — 좌표 VO (latitude, longitude)

### auth
- `auth/domain/Member.java` — 회원 도메인 모델
- `auth/domain/MemberRepository.java` — 회원 저장소 인터페이스
- `auth/domain/RefreshToken.java` — 리프레시 토큰 도메인 모델
- `auth/infrastructure/persistence/MemberJpaEntity.java` — 회원 JPA 엔티티
- `auth/infrastructure/persistence/MemberRepositoryImpl.java` — 회원 저장소 구현체
- `auth/infrastructure/social/KakaoAuthClient.java` — 카카오 OAuth 클라이언트
- `auth/application/AuthService.java` — 인증 유스케이스
- `auth/presentation/AuthController.java` — 인증 REST API

### member
- `member/domain/departure/DeparturePlace.java` — 출발지 도메인 모델
- `member/domain/terms/Terms.java`, `TermsAgreement.java` — 약관 도메인 모델
- `member/infrastructure/...` — JPA 엔티티 + 구현체
- `member/application/MemberService.java`, `DeparturePlaceService.java`, `TermsService.java`
- `member/presentation/MemberController.java`, `DeparturePlaceController.java`, `TermsController.java`
