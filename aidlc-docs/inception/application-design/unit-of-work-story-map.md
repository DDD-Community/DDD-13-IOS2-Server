# Unit of Work — Story Map

> User Stories 단계를 스킵했으므로, 기능 요구사항(FR)을 유닛에 매핑합니다.

## Unit 1: Global

| 요구사항 | 설명 |
|---|---|
| (인프라) | JWT 생성/검증, Security 설정, 에러 처리, 값객체 |

## Unit 2: Auth

| 요구사항 | 설명 |
|---|---|
| FR-01.1 | 카카오, 네이버, 애플 소셜 로그인 |
| FR-01.2 | 공급자별 토큰 검증 |
| FR-01.3 | JWT 발급 (access 1h / refresh 30d) |
| FR-01.4 | Refresh Token 해시 저장 |
| FR-01.5 | 신규 회원 플래그 |
| FR-01.6 | 한 회원 = 한 소셜 계정 |
| FR-01.7 | provider + socialUserId 복합 유니크 |

## Unit 3: Member

| 요구사항 | 설명 |
|---|---|
| FR-02.1 | 닉네임 2~20자, 중복 허용 |
| FR-02.2 | 금칙어 필터 |
| FR-02.3 | 프로필 이미지 nullable |
| FR-02.4 | 기본 출발지 1개 필수 |
| FR-02.5 | 필수 약관 미동의 시 거부 |
| FR-03.1~4 | 출발지 CRUD + PostGIS |
| FR-04.1~4 | 약관 버전 관리 + 재동의 + DELETE 금지 |
| FR-05.1~2 | 디바이스 토큰 저장/갱신/삭제 |
| FR-06.1~2 | Swagger UI (local/dev만) |

## 커버리지

모든 FR이 유닛에 매핑됨. 누락 없음.
