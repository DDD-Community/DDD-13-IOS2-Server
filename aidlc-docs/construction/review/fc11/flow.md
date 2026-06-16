# 처리 흐름 — FC-11 투표 생성

## 단계
1. 트리거(호스트 생성 / 자동전환)
2. durationDays 검증(1·3·7) → 마감일 계산(23:59:59)
3. 유효성: 시작 < 마감 < 약속일
4. meeting_place_vote_session 저장
5. 이동부담 스냅샷 생성(FC-12 참조)
6. locationStatus → VOTING

## 상태 전이
- RECOMMENDED → VOTING
