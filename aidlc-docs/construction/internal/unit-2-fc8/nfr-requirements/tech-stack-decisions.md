# U2 — Tech Stack Decisions

- 신규 의존성 추가 없음. 기존 PostGIS(geography), Hibernate 6 native query, Spring Data JPA 패턴 그대로 사용
- `place` 컨텍스트는 읽기 전용 리포지토리만 구현(쓰기 경로는 별도 데이터 파이프라인, 이번 유닛 범위 외)
