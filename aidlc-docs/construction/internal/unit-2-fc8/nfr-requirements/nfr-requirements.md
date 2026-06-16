# U2 추천(FC-8) — NFR Requirements (경량)

> 근거: execution-plan.md NFR rationale(PostGIS 추천 성능) + 소규모 서비스 규모(place ~2천건, Cloud Run 단일~소수 인스턴스)

## 성능
- `location/start` 1회 호출 내 PostGIS 반경검색(최대 3회 사다리 반복) + 스코어링(메모리, ~2천건 이하) → 목표 500ms 이내, 동기 처리로 충분 (호스트가 누르는 저빈도 액션)
- 기존 인덱스 재사용: `idx_place_location`(GIST), `idx_place_vibe`(GIN). `category_label`은 카디널리티 낮고 데이터 적어 신규 인덱스 불필요

## 보안
- 기존 JWT 인증 + 호스트 권한 체크(GroupMemberRole.HOST) 패턴 재사용. 신규 보안 요구사항 없음

## 신뢰성
- 상태전이(`completeRecommendation`) + 스냅샷 저장은 동일 트랜잭션(서비스 메서드 단위) — 부분 실패 시 롤백
- 반경 사다리 6km 소진 시 명확한 에러(`PLACE_RECOMMENDATION_EMPTY`, `MIDPOINT_STATION_NOT_FOUND`)로 종료

## 확장성/가용성
- 기존 Cloud Run 오토스케일 그대로 사용. 신규 인프라/배포 변경 없음

## 유지보수성
- `PlaceScorer`는 순수함수로 분리 → 단위테스트 + PBT(정규화 단조성: rating 높을수록 점수 비감소) 대상
