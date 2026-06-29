# Application Design — FC-8 보완: 장소 상세 응답 보강

## 개요
- read-only 필드 추가. 새 컴포넌트/서비스 없음. 기존 `place` 컨텍스트 4개 파일 + Flyway 1개만 변경.
- 비즈니스 로직 없음 → Functional Design 단계 사실상 매핑 추가뿐.

## 변경 대상 컴포넌트

| 레이어 | 파일 | 변경 |
|---|---|---|
| migration | `db/migration/V32__add_place_detail_fields.sql` | **신규** — ALTER ADD COLUMN 3종 |
| infrastructure | `place/infrastructure/persistence/PlaceJpaEntity.java` | 필드 4개 추가 + `toDomain()` 매핑 |
| domain | `place/domain/Place.java` | 필드 4개 추가 + 빌더 |
| presentation | `place/presentation/dto/PlaceDetailResponse.java` | 필드 4개 추가 + `from()` 매핑 |

> `PlaceController.getPlaces`, `PlaceRepository.findByIds`는 **시그니처 변경 없음** — 도메인 객체가 풍부해지면 응답에 자동 반영.

## 스키마 (V32)
```sql
ALTER TABLE place ADD COLUMN IF NOT EXISTS road_address   TEXT;
ALTER TABLE place ADD COLUMN IF NOT EXISTS business_hours TEXT;
ALTER TABLE place ADD COLUMN IF NOT EXISTS holiday        TEXT;
COMMENT ON COLUMN place.address        IS '지번주소';
COMMENT ON COLUMN place.road_address   IS '도로명주소';
COMMENT ON COLUMN place.business_hours IS '영업시간 표시용 원문';
COMMENT ON COLUMN place.holiday        IS '휴무 표시용 원문';
```
- 모두 nullable. `address` 의미만 도로명→지번으로 변경(타입 동일, 스키마 변경 아님 — COMMENT만 갱신)
- 데이터 적재(TRUNCATE+import / UPDATE)는 사용자 콘솔 작업

## 도메인 / 엔티티
`Place`, `PlaceJpaEntity` 공통 추가 필드:
- `String roadAddress` ← `road_address`
- `String businessHours` ← `business_hours`
- `String holiday` ← `holiday`
- `String naverUrl` ← `naver_url` (**기존 컬럼**, 매핑만 신규)

## 응답 DTO
`PlaceDetailResponse` 추가 필드: `roadAddress`, `businessHours`, `holiday`, `naverUrl`
- `address`는 기존 필드 유지(이제 지번주소 의미)
- 값 없으면 null

```json
{
  "placeId": 12, "name": "○○식당", "categoryLabel": "한식",
  "address": "서울특별시 성동구 성수동2가 315-29",
  "roadAddress": "서울특별시 성동구 연무장5가길 20-1",
  "latitude": 37.5, "longitude": 127.0,
  "vibe": ["분위기좋은"], "occasion": ["회식"],
  "reservable": true, "hasParking": false, "rating": 4.3,
  "businessHours": "월~금 11:00-20:30 (브레이크 15:30-17:00), 토~일 11:30-20:30",
  "holiday": "연중무휴",
  "naverUrl": "https://map.naver.com/p/entry/place/12"
}
```

## 영향 범위 / 비영향
- 영향: `GET /api/v1/places?ids=` 응답만 확장
- 비영향: `PlaceSummary`, `PlaceNearbyResponse`, `recommendations`, `options` — 변경 없음
- 스코프 제외: 출발지 거리, 함께담기 N, 딥링크 조립

## 의존성 / 통신
- 추가 의존성 없음. 기존 `presentation → application(없음, 컨트롤러 직접 repo) → domain ← infrastructure` 흐름 유지
- (참고) `PlaceController`가 `PlaceRepository`를 직접 사용하는 기존 구조 그대로
