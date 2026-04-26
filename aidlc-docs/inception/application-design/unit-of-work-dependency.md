# Unit of Work — Dependency Matrix

## 의존성

| Unit | 의존 대상 | 이유 |
|---|---|---|
| Unit 1: Global | 없음 | 기반 인프라 |
| Unit 2: Auth | Unit 1 (Global) | JwtProvider, SecurityConfig 사용 |
| Unit 3: Member | Unit 1 (Global), Unit 2 (Auth) | 공통 인프라 + member 테이블 |

## 구현 순서

```
Unit 1: Global  →  Unit 2: Auth  →  Unit 3: Member
```

순차 의존. 병렬 구현 불가.

## 테이블 생성 순서 (Flyway)

| 순서 | 마이그레이션 | 테이블 | Unit |
|---|---|---|---|
| V1 | PostGIS 확장 활성화 | - | Global |
| V2 | member, refresh_token | `member`, `refresh_token` | Auth |
| V3 | departure_place | `departure_place` | Member |
| V4 | terms, terms_agreement | `terms`, `terms_agreement` | Member |
| V5 | device_token | `device_token` | Member |
| V6 | 약관 시드 데이터 | `terms` INSERT | Member |
