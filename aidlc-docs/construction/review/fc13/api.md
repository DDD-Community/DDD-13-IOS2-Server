# API 명세 — FC-13 자동 확정

## (시스템) 자동 확정
- 전원 투표완료 후처리 또는 투표마감 스케줄러가 수행. 직접 호출 엔드포인트 없음

## GET /api/v1/meetings/{meetingId}/place-result
- 확정 장소(placeId, name, address) + 최종 득표/이동부담 요약
- 미확정 시 PLACE_NOT_CONFIRMED(400) 또는 진행상태 반환
