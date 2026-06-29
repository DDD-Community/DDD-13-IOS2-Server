# Requirement Verification Questions -- Radius-based Nearby Place Search

## Q1. 검색 기준점 (center point)
지도에서 반경 검색할 때 기준점은 무엇인가요?

A) 클라이언트가 전달하는 임의 좌표 (latitude, longitude) -- 지도 중심점
B) 사용자의 현재 위치 좌표
C) 기존 중간역(midpoint station) 좌표를 활용
D) X) 기타 (아래에 설명)

[Answer]: A

## Q2. 필터링 조건
반경 검색 시 필터 조건이 필요한가요? (복수 선택 가능)

A) 카테고리(categoryLabel) 필터
B) 분위기(vibe) 필터
C) 용도(occasion) 필터
D) 예약 가능(reservable) / 주차 가능(hasParking) 필터
E) 필터 없음 -- 반경 내 모든 장소 반환
F) X) 기타 (아래에 설명)

[Answer]: [img.png](img.png) A는 필터, B와 D는 응답에 추가 태그형식

## Q3. 반경(radius) 범위
반경 값의 제한이 필요한가요?

A) 기본값 + 최대값 설정 (예: 기본 500m, 최대 3km)
B) 클라이언트가 자유롭게 지정 (제한 없음)
C) 고정 프리셋만 허용 (예: 300m / 500m / 1km / 3km)
D) X) 기타 (아래에 설명)

[Answer]: B

## Q4. 응답 형식과 정렬
응답 데이터 형식과 정렬 기준은?

A) 기존 PlaceDetailResponse와 동일 + 거리(distance) 필드 추가, 거리순 정렬
B) 기존 PlaceDetailResponse와 동일, 정렬 기준 없음
C) 새로운 응답 형식 필요 (아래에 설명)
D) X) 기타 (아래에 설명)

[Answer]: a

## Q5. 페이지네이션
결과가 많을 경우 페이지네이션이 필요한가요?

A) 커서 기반 페이지네이션
B) 오프셋 기반 페이지네이션 (page + size)
C) 건수 제한만 (예: 최대 50건)
D) 페이지네이션 불필요 -- 전건 반환
E) X) 기타 (아래에 설명)

[Answer]: C

## Q6. 인증 요구
이 API는 로그인 사용자만 호출 가능한가요?

A) 인증 필수 (기존 API처럼 JWT 필요)
B) 인증 불필요 (공개 API)
C) X) 기타 (아래에 설명)

[Answer]: A

## Q7. 모임(meeting) 연관 여부
이 API는 특정 모임과 연관되나요, 아니면 독립적인 장소 검색인가요?

A) 모임과 무관 -- 독립적인 장소 검색 (place 컨텍스트)
B) 특정 모임 컨텍스트에서 사용 -- meetingId 필요
C) X) 기타 (아래에 설명)

[Answer]: A

## Q8. 기존 FC 번호 매핑
이 기능은 기존 FC 번호를 확장하는 것인가요, 새 FC를 할당하나요?

A) 기존 FC-8 (장소 추천) 확장
B) 새 FC 번호 할당 (예: FC-14)
C) X) 기타 (아래에 설명)

[Answer]: A
