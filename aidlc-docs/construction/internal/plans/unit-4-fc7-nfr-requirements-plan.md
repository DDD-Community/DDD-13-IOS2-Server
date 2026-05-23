# Unit 4 (FC-7) NFR Requirements Plan

## 기 결정 사항 (requirements.md 기준)

- **Security Baseline**: 호스트/구성원/본인 인가 분리, 미인가 403
- **스케줄러**: @Scheduled 매일 자정 (투표 자동 확정 + 모임 자동 종료)
- **FCM**: 코드 완전 구현, Firebase 키 설정은 별도 태스크
- **데이터 제약**: 후보 최대 3개, durationDays 1/3/7, 구성원 최대 20명
- **SSE 미사용**: GET 폴링 방식

## 미결 NFR 질문

## Question 1
스케줄러(@Scheduled) 실행 중 예외 발생 시 처리 전략은?

A) 예외 로깅만 하고 다음 대상으로 계속 진행 (한 건 실패가 전체 배치 중단 안 함)
B) 예외 발생 시 전체 배치 롤백 후 다음 자정 재시도
C) Other (please describe after [Answer]: tag below)

[Answer]: A
## Question 2
FCM 알림 발송 실패 시 처리 전략은?

A) 로깅만 하고 무시 (알림은 best-effort, 핵심 기능 아님)
B) 재시도 로직 추가 (예: 3회 재시도)
C) Other (please describe after [Answer]: tag below)

[Answer]: C 여기서 FCM안할꺼야
