# 요구사항 검증 질문 — FC-8 보완: 장소 상세 응답 보강

> 대상 API: `GET /api/v1/places?ids={...}` (`PlaceController.getPlaces` → `PlaceDetailResponse`)
> 목적: 장소 상세 바텀시트(PRD `mvp3.md` §9-3, `mvp3-1.md` §12-1.1)가 요구하는 필드를 응답에 추가
> 채팅에서 합의된 부분은 **[합의됨]** 으로 표기. 나머지만 답해줘.

---

## A. 추가 필드 / 컬럼 (대부분 합의됨 — 확인만)

**A-1. 영업시간** — `business_hours TEXT` 단일 텍스트(표시용, 백엔드 파싱 없음). [합의됨]
- 예: `"월~금 11:00-20:30 (브레이크 15:30-17:00), 토~일 11:30-20:30"`
- [Answer]:  예

**A-2. 휴무** — `holiday TEXT` 단일 텍스트. [합의됨]
- 예: `"연중무휴"`, `"월 휴무"`
- [Answer]: 예

**A-3. 주소 컬럼 의미** — 기존 `address`의 의미를 **지번주소**로 변경, `road_address`(도로명) 신규 추가. [합의됨]
- `address` = 지번주소, `road_address` = 도로명주소
- (재import 시 export 스크립트 매핑도 같이 변경)
- [Answer]: 예

**A-4. 네이버 지도 링크** — 기존 `naver_url`(이미 DB·CSV에 존재, 응답엔 미노출)을 응답에 추가. [합의됨]
- [Answer]: 예

---

## B. 스코프 경계 (확정 필요)

**B-1. 출발지 기준 거리** — 디자인엔 "유저 출발지로부터 장소 거리"가 있음. 이 상세 API(`?ids=`)는 기준좌표를 안 받음.
이번 작업 스코프에 **포함할지**?
- A) **제외** — 상세 API는 좌표 비의존 순수 조회 유지. 거리는 별도(목록/거리보기) 책임. (권장)
- B) 포함 — `?ids=` 에 `fromLat`/`fromLng` 옵션 추가해 거리 계산 반환
- X) 기타: 
- [Answer]: X 이거는 범용으로 쓰는 api 이기기도하고 매번 이 거리 구하는게 좀 리소스가 나올거같으면 이거는 빼도 될것같아

**B-2. "함께 담기 N"(해당 장소 담은 모임원 수)** — 모임 맥락 데이터(meeting_place_pick)라 places 단독 조회로는 불가.
- A) **이번 스코프 제외** — 별도 meeting API 책임(이미 담기현황 API 존재). (권장)
- B) 포함
- X) 기타:
- [Answer]: A

**B-3. 네이버 링크 형태** — 응답에 어떤 형태로 줄지?
- A) **`naver_url`(https 원본 URL) 그대로** 노출 + 기존 `placeId`도 있으니 프론트가 딥링크(`nmap://place?id=`) 조립. (권장)
- B) 백엔드가 `nmap://place?id={placeId}` 딥링크 문자열을 만들어서 반환
- C) 둘 다(url + deeplink)
- X) 기타:
- [Answer]: A

---

## C. 데이터/적재

**C-1. 컬럼 추가 방식** — 기존 V12 수정 금지, 신규 **Flyway V32**(`ALTER TABLE place ADD COLUMN IF NOT EXISTS road_address/business_hours/holiday TEXT`). [합의됨]
- [Answer]: 예

**C-2. 데이터 적재** — 컬럼 생성 후 사용자가 콘솔에서 `TRUNCATE place` → 새 CSV import, 또는 준비한 UPDATE 쿼리 직접 실행. (스키마 아닌 데이터라 수동 OK) [합의됨]
- [Answer]: 예

**C-3. export 스크립트(`Data/pipeline/lib/export_for_gcp.py`) 수정** — 새 컬럼(road_address/business_hours/holiday) + address 의미변경(지번) 반영을 누가?
- A) 내가(Claude) 같이 수정
- B) 사용자가 직접 (CSV 이미 준비 완료)
- [Answer]: B

---

## D. 비기능 / 확장 (기존 사이클 설정 유지)

- Security baseline: Yes, Property-Based Testing: Partial, TDD: No — 기존 설정 그대로 유지할지?
- [Answer]: 뭐야이건;

---

## E. 빠진 항목 / 추가 의견

- [Answer]: 
