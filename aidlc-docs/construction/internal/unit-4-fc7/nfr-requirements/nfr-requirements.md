# NFR Requirements — Unit 4 (FC-7)

## 보안 (Security Baseline)

| API | 인가 조건 |
|---|---|
| GET /meetings/{id} | 그룹 멤버 |
| POST /date-vote/host-pick | 그룹 HOST |
| POST /date-vote | 그룹 HOST |
| POST /date-vote/submit | 그룹 멤버 |
| GET /date-vote | 그룹 멤버 |
| PATCH /date-vote/confirm | 그룹 HOST |

미인가 → 403 반환

## 스케줄러

- 실행 주기: 매일 자정 (`@Scheduled(cron = "0 0 0 * * *")`)
- 예외 처리: 건별 try-catch, 예외 로깅 후 다음 대상 계속 진행 (한 건 실패가 전체 배치 중단 안 함)
- 스케줄러 실패 시 대안: 호스트가 `confirm` 또는 `host-pick` API로 수동 처리 가능

## 데이터 제약

| 항목 | 제약 |
|---|---|
| 후보 날짜 수 | 1~3개 |
| durationDays | 1 / 3 / 7 |
| 구성원 최대 | 20명 |
| 출발지 표시 | N개 전체 |

## 알림 (FCM)

- MVP1 제외 — 추후 별도 유닛으로 구현
- FC-7 코드에서 FCM 호출부는 no-op 처리

## 성능

- 데이터 규모가 작아 (세션 1개, 옵션 최대 3개, 레코드 최대 60행) 특별한 최적화 불필요
- 표준 JPA 쿼리로 충분
