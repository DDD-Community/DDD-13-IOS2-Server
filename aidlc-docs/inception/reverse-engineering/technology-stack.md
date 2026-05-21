# Technology Stack

## 언어 & 프레임워크
| 항목 | 버전 | 용도 |
|---|---|---|
| Java | 17 | 기본 언어 |
| Spring Boot | 3.4.4 | 웹 프레임워크 |
| Spring Security | (Boot 관리) | JWT 필터 체인, 인증 |
| Spring Data JPA | (Boot 관리) | ORM |
| Spring Validation | (Boot 관리) | 입력값 검증 |

## 데이터
| 항목 | 버전/설정 | 용도 |
|---|---|---|
| PostgreSQL | (런타임) | 기본 DB |
| PostGIS | V1 init | 좌표(지리) 타입 지원 |
| Flyway | (Boot 관리) | 스키마 마이그레이션 |

## 라이브러리
| 항목 | 버전 | 용도 |
|---|---|---|
| Lombok | (Boot 관리) | 보일러플레이트 제거 |
| JJWT | 0.12.6 | JWT 생성·검증 |
| Springdoc OpenAPI | 2.8.6 | Swagger UI |
| spring-dotenv | 4.0.0 | .env 파일 지원 |

## 빌드
| 항목 | 용도 |
|---|---|
| Gradle | 빌드·의존성 관리 |

## 현재 DB 스키마 (Flyway 마이그레이션 현황)
| 버전 | 파일 | 내용 |
|---|---|---|
| V1 | init_postgis | PostGIS 확장 활성화 |
| V2 | create_member_and_refresh_token | member, refresh_token 테이블 |
| V3 | create_departure_place | departure_place 테이블 |
| V4 | create_terms | terms, terms_agreement 테이블 |
| V5 | create_device_token | device_token 테이블 (푸시 알림용) |
| V6 | seed_terms | 약관 초기 데이터 |
