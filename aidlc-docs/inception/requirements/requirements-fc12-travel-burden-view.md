# Requirements — FC-12 보완: 친구들 거리보기 응답 보강

> 2026-06-25. 기존 FC-12(친구들 거리보기, R9) 확장. 새 FC 아님 — fc12 폴더 갱신.

## 배경 / 문제
- 직전 사이클에서 `meeting_travel_burden`에 **이동경로(station_path)** 까지 확보 완료.
- 현재 `getPlaceTravelBurden`는 **burden 스냅샷이 있는 멤버만** 반환 → 좌표 미설정/스냅샷 누락 멤버가 빠짐.
- 응답에 **출발지 이름**, **요청자 본인 여부(isMe)** 가 없어 프론트가 구분/표기 어려움.

## 목표
- 친구들 거리보기 = 단일 장소에 대해 **해당 모임의 활성 참여자 전원**(요청자 포함)을 노출.
- 멤버별로 **출발지 이름 + 본인 여부 + 이동경로 + 소요시간/환승** 제공.
- 데이터는 기존 스냅샷(`meeting_travel_burden`) 재사용. 신규 계산 없음.

## 기능 요구사항
- R-V1: 응답 멤버 목록 = 모임 **활성 참여자(ABSENT 제외) 전원**. 요청자 본인 포함.
- R-V2: burden 스냅샷이 없는 멤버도 포함한다. 이 경우 `seconds`/`transfers`=null, `path`=[].
- R-V3: 멤버별 `isMe`(요청자 본인 여부) 제공.
- R-V4: 멤버별 `departureName`(출발지 이름) 제공.
  - 참여자 좌표를 멤버의 `DeparturePlace`와 매칭 → `placeName`(없으면 `label`).
  - 좌표 매칭 실패 시 멤버 기본 출발지 이름, 그래도 없으면 null.
- R-V5: 멤버별 `path`(이동경로 역 좌표 리스트) 제공 (기존 확보분).
- R-V6: 최장 이동자 플래그(`isLongest`)는 **소요시간이 있는 멤버들 중** 최대값에만 true.

## 비기능 / 제약
- 신규 계산/다익스트라 없음 — 스냅샷 + 멤버/출발지 조회만.
- 스키마 변경 없음 (출발지 이름은 읽기 시점 해석).
- 기존 API 경로/장소 응답 구조 유지, 멤버 항목 필드만 확장.

## 영향 범위
- `PlaceTravelBurdenResponse.MemberBurden` 필드 확장(departureName, isMe, nullable seconds/transfers).
- `PlaceVoteService.getPlaceTravelBurden` 로직: 참여자 기준 조립 + 출발지 매칭.
- `DeparturePlaceRepository` 의존 추가(PlaceVoteService).

## 결정사항
1. 멤버 기준 = **활성 참여자(ABSENT 제외)**. (FC-12 전반 규칙과 일치)
2. 출발지 이름 = 읽기 시점 좌표매칭 해석. 스키마 추가 없음.
3. `seconds`/`transfers` nullable → 스냅샷 없는 멤버 구분.
4. isLongest = 소요시간 보유 멤버 한정 최대.
