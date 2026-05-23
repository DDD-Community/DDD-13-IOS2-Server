# Code Generation Plan — Unit 4 (FC-7-1) 내 정보 수정

## 구현 범위

- `PATCH /api/v1/groups/{groupId}/members/me/attendance` — 참석여부 수정
- `POST /api/v1/departure-places` — 출발지 추가 (최대 3개)
- `PUT /api/v1/departure-places/{id}` — 출발지 수정

---

## 생성/수정 파일 목록

### 신규 생성
```
src/main/java/com/bangawo/
├── group/
│   ├── application/
│   │   └── GroupMemberService.java          ← 참석여부 수정 서비스
│   └── presentation/
│       ├── GroupMemberController.java        ← PATCH /api/v1/groups/{groupId}/members/me/attendance
│       └── dto/
│           └── AttendanceUpdateRequest.java  ← 참석여부 요청 DTO
└── member/
    └── presentation/
        └── dto/
            └── DeparturePlaceRequest.java    ← 출발지 추가/수정 요청 DTO (공용)
```

### 수정
```
group/domain/GroupMember.java                        ← + updateAttendance()
member/domain/departure/DeparturePlace.java           ← + update()
member/application/DeparturePlaceService.java         ← MAX_PLACES 3으로 수정 + update() + 방어로직
member/presentation/DeparturePlaceController.java     ← + POST, PUT 엔드포인트
```

---

## 실행 단계

- [x] **Step 1** — Domain: `GroupMember` + `updateAttendance(AttendanceStatus)` 메서드 추가
- [x] **Step 2** — Domain: `DeparturePlace` + `update(label, address, coordinate)` 메서드 추가
- [x] **Step 3** — Application: `GroupMemberService` (신규) — 참석여부 수정 로직
- [x] **Step 4** — Application: `DeparturePlaceService` 수정 — MAX_PLACES=3, update() 추가, 첫 등록 방어로직
- [x] **Step 5** — DTO: `AttendanceUpdateRequest` (신규)
- [x] **Step 6** — DTO: `DeparturePlaceRequest` (신규, 추가/수정 공용)
- [x] **Step 7** — Presentation: `GroupMemberController` (신규) — PATCH 엔드포인트
- [x] **Step 8** — Presentation: `DeparturePlaceController` 수정 — POST, PUT 엔드포인트 추가
- [x] **Step 9** — Review: `aidlc-docs/construction/review/fc7-1/api.md` (신규)

---

## 주요 설계 결정

| 항목 | 결정 |
|---|---|
| 참석여부 API 경로 | `PATCH /api/v1/groups/{groupId}/members/me/attendance` (Q1=A) |
| isDefault 처리 | 클라이언트 명시 + 첫 등록 시 서버 강제 true (Q2=A + 방어) |
| 출발지 수정 필드 | label + address + latitude + longitude (Q3=A) |
| isDefault 수정 | 불가 — PUT에서 장소 정보만 (Q4=B) |
| 출발지 최대 개수 | 3개 (기존 MAX_PLACES=10 → 3으로 수정) |
| Flyway 마이그레이션 | 불필요 (신규 테이블 없음) |
