# Tech Stack Decisions — Unit 4 (FC-7)

## 기존 스택 그대로 사용

| 항목 | 결정 | 이유 |
|---|---|---|
| 프레임워크 | Spring Boot 3.4.4 | 기존 동일 |
| ORM | Spring Data JPA | 기존 동일 |
| DB | PostgreSQL (Cloud SQL) | 기존 동일 |
| 스케줄러 | Spring @Scheduled | 외부 의존 없이 단순 구현 가능 |
| 알림 | 없음 (MVP1 제외) | FCM 추후 별도 유닛 |
| 실시간 | GET 폴링 | SSE 불필요로 결정 |

## 신규 결정 사항 없음

FC-7은 기존 스택 위에서 표준 CRUD + @Scheduled 조합으로 구현. 신규 라이브러리 추가 없음.
