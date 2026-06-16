# Application Design Plan — FC-8~13

## 산출물 체크리스트
- [ ] components.md — 컴포넌트 정의·책임
- [ ] component-methods.md — 메서드 시그니처
- [ ] services.md — 애플리케이션 서비스·오케스트레이션
- [ ] component-dependency.md — 의존 관계·통신·데이터 흐름
- [ ] application-design.md — 통합 문서
- [ ] 설계 일관성 검증

---

## 결정 질문 (대부분 ✅추천 — "추천대로" 가능)

### AD-Q1. 추천 스코어링 가중치/정규화 (★ 약속한 확인 지점)
후보 장소 집합 내 min-max 정규화 후 가중합:
`score = w1·(1 - dist_norm) + w2·tag_match + w3·rating_norm`
- dist_norm = 참여자 전원→장소 직선거리 평균을 후보집합 min-max 정규화 (가까울수록 1)
- tag_match = (모임 themeTag·옵션 ∩ place.vibe/occasion/category) 매칭 비율 0~1
- rating_norm = place.rating 후보집합 min-max (없으면 0.5 중립)

가중치 기본값:
- A) **w1=0.5, w2=0.4, w3=0.1** (거리 우선, 태그 보조) ✅추천
- B) w1=0.4, w2=0.5, w3=0.1 (태그 우선)
- C) 직접 지정

[Answer]: 

### AD-Q1-1. rating/태그 데이터가 비어있는 장소 처리
- A) 해당 항만 중립(0.5/0)으로 계산, 후보 유지 ✅추천
- B) 데이터 없으면 추천 제외
- X) 기타

[Answer]: 

### AD-Q2. place — 신규 바운디드 컨텍스트로?
- A) `com.bangawo.place` 신규 컨텍스트 (도메인/인프라/추천 리포) ✅추천
- B) subway 컨텍스트 하위에 포함
- X) 기타

[Answer]: 

### AD-Q3. 추천 스코어링 실행 위치
참여자 거리집계·태그매칭은 PostGIS 쿼리 + 일부 서버 계산.
- A) place 컨텍스트 Repository의 **PostGIS 네이티브 쿼리에서 거리·필터**, 서버에서 태그·정규화·정렬 ✅추천
- B) 전부 서버 메모리 계산
- C) 전부 SQL
- X) 기타

[Answer]: 

### AD-Q4. subway 그래프(이동시간·환승) 컴포넌트 위치
- A) `subway` 컨텍스트에 `SubwayGraph`(부팅 시 subway_edge 로드) + `ShortestPathService`(다익스트라). meeting이 포트로 호출 ✅추천
- B) meeting 컨텍스트에 둠
- X) 기타

[Answer]: 

### AD-Q5. 신규 테이블 명명 (V18~)
- A) `meeting_place_recommendation`(추천스냅샷) / `meeting_place_pick`(담기) / `meeting_place_vote`(투표) / `meeting_place_vote_session`(투표세션·마감일) / `meeting_travel_burden`(이동부담스냅샷) / `meeting_confirmed_place`(확정) ✅추천
- B) 직접 명명
- X) 기타

[Answer]: 

### AD-Q6. 이동부담 스냅샷 시점 재확인
- A) 투표 세션 생성(VOTING 진입) 시 1회 계산·저장 ✅추천(확정됨)
- X) 기타

[Answer]: 

### AD-Q7. locationStatus enum 교체 시 기존 데이터
기존 모임의 locationStatus(BEFORE/IN_PROGRESS/COMPLETED) 매핑.
- A) Flyway로 IN_PROGRESS→RECOMMENDED, COMPLETED→CONFIRMED 데이터 마이그레이션 ✅추천
- B) 신규 모임만 적용, 기존은 BEFORE 리셋
- X) 기타

[Answer]: 
