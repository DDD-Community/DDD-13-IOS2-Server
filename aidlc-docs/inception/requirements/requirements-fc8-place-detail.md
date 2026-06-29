# Requirements — FC-8 보완: 장소 상세 응답 보강

## Intent Analysis
- **User request**: 장소 상세 조회 API(`GET /api/v1/places?ids=`) 응답에, 디자인/PRD가 요구하는 영업시간·휴무·도로명/지번 주소·네이버 지도링크를 추가한다.
- **Request type**: Enhancement (기존 기능 확장)
- **Scope estimate**: Single Component — `place` 컨텍스트(domain/infra/presentation) + `place` 테이블 컬럼 추가
- **Complexity**: Simple — read-only 필드 추가, 비즈니스 로직 없음
- **FC 매핑**: **FC-8 확장** (장소 상세 API는 `fc8/api.md`에 문서화됨). 새 폴더 금지, fc8 갱신.
- **PRD 근거**: `docs/prd/mvp3.md` §9-3 장소 상세, `docs/prd/mvp3-1.md` §12-1.1 장소 상세 바텀시트

## 배경
현재 `PlaceDetailResponse`는 `placeId, name, categoryLabel, address, latitude, longitude, vibe[], occasion[], reservable, hasParking, rating` 만 반환.
PRD 장소 상세 바텀시트가 요구하는 **영업시간/휴무, 도로명+지번 주소, 네이버 지도링크**가 빠져 있어 상세 화면을 못 채운다.
- `naver_url`은 이미 `place` 테이블·CSV에 존재하나 도메인~응답까지 매핑 안 됨.
- 영업시간/휴무/지번주소는 컬럼 자체가 없음 → 신규 컬럼 + 데이터 적재 필요.
- 기존 `address` 컬럼은 도로명주소를 담고 있었음 → 의미를 **지번주소**로 변경하고 도로명은 신규 `road_address`로 분리.

## Functional Requirements

### FR-1. place 테이블 컬럼 추가 (Flyway V32)
- `road_address TEXT` (도로명주소), `business_hours TEXT` (영업시간 표시용 원문), `holiday TEXT` (휴무 표시용 원문) 추가
- 기존 V12 수정 금지 — 신규 마이그레이션 `V32`로 `ALTER TABLE place ADD COLUMN IF NOT EXISTS ...`
- 모든 컬럼 nullable (데이터 미적재 상태에서도 앱 정상 동작)

### FR-2. address 의미 변경
- 기존 `address` 컬럼의 의미: 도로명주소 → **지번주소**
- 데이터 재적재 시점에 값 교체(사용자 UPDATE/재import). 스키마 변경 아님(타입 동일 TEXT)

### FR-3. 도메인/엔티티 매핑 확장
- `PlaceJpaEntity`, `Place` 도메인에 `roadAddress`, `businessHours`, `holiday`, `naverUrl` 필드 추가
- `naverUrl`은 기존 컬럼(`naver_url`) 매핑만 추가 (스키마 변경 없음)

### FR-4. PlaceDetailResponse 확장
- 추가 필드: `roadAddress`, `businessHours`, `holiday`, `naverUrl`
- 기존 `address`는 이제 지번주소를 의미 (필드명 유지, 의미만 변경)
- 적용 API: `GET /api/v1/places?ids=` (`PlaceController.getPlaces`)

### FR-5. 미적재/미존재 처리
- 신규 필드 값이 없으면 `null` 반환 (에러 아님)
- 기존 동작 유지: 요청 순서 보존, 미존재 placeId는 결과에서 제외

## Scope 경계 (확정)
- **거리(출발지 기준): 제외** — 범용 read-only API이며 매 호출 거리 계산은 리소스 부담. 거리는 목록/거리보기 책임 (B-1)
- **"함께 담기 N": 제외** — 모임 맥락(meeting_place_pick) 데이터. 기존 담기현황 API 책임 (B-2)
- **네이버 링크: `naver_url` 원본 URL 그대로** 노출 — 딥링크(`nmap://place?id=`)는 프론트가 placeId로 조립 (B-3)
- **export 스크립트(`Data/pipeline`) 수정: 사용자 직접** — CSV 이미 준비 완료 (C-3, Server repo 스코프 외)
- 영향 받는 다른 응답: `PlaceSummary`, `PlaceNearbyResponse`, 추천(`recommendations`) 응답은 **변경 없음** — 상세 API만 보강

## Non-Functional Requirements
- 기존 확장 설정 유지: Security baseline = Yes, Property-Based Testing = Partial, TDD = No (변경 없음)
- 하위호환: 기존 응답 필드/구조 유지하며 필드만 추가 (additive). 단 `address` 값의 의미가 도로명→지번으로 바뀌는 점은 프론트와 공유 필요
- place 도메인은 read-only (애플리케이션에 쓰기 경로 없음, 외부 파이프라인 적재) 원칙 유지

## 데이터/적재 (사용자 책임, Server 스코프 외)
- V32로 컬럼 생성 후, 사용자가 콘솔에서 `TRUNCATE place` → 새 CSV import 또는 준비된 UPDATE 쿼리 실행
- export 스크립트(`export_for_gcp.py`) 컬럼/매핑 변경은 사용자가 처리

## Key Requirements 요약
1. V32: place에 `road_address`/`business_hours`/`holiday` TEXT 추가
2. `address` 의미 = 지번주소, `road_address` = 도로명주소
3. `naver_url` 매핑 추가(스키마 변경 없음)
4. `PlaceDetailResponse`에 `roadAddress`/`businessHours`/`holiday`/`naverUrl` 추가
5. 상세 API(`?ids=`)에만 적용. 거리·함께담기 N은 스코프 제외
