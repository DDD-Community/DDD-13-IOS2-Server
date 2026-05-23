# Code Generation Plan — Unit 3 (FC-6) 모임 리스트

## 구현 범위

`GET /api/v1/meetings` — JWT 인증된 사용자의 모임 카드 목록 반환

---

## 생성/수정 파일 목록

### 신규 생성 — meeting 컨텍스트
```
src/main/java/com/bangawo/meeting/
├── application/
│   └── MeetingListService.java          ← 오케스트레이션 서비스
└── presentation/
    ├── MeetingController.java            ← GET /api/v1/meetings
    └── dto/
        └── MeetingCardResponse.java      ← 응답 DTO (inner class MemberInfo 포함)
```

### 수정 — 도메인 인터페이스 (메서드 추가)
```
meeting/domain/MeetingRepository.java         ← + findLatestByGroupIdIn
group/domain/GroupMemberRepository.java       ← + findByMemberId, findByGroupIdIn
group/domain/GroupRepository.java             ← + findAllById
group/domain/ThemeTagRepository.java          ← + findByCodeIn
auth/domain/MemberRepository.java             ← + findAllById
```

### 수정 — 인프라 (구현체 추가)
```
meeting/infrastructure/persistence/MeetingJpaRepository.java     ← + @Query (JPQL subquery)
meeting/infrastructure/persistence/MeetingRepositoryImpl.java    ← + 구현
group/infrastructure/persistence/GroupMemberJpaRepository.java   ← + 쿼리 메서드
group/infrastructure/persistence/GroupMemberRepositoryImpl.java  ← + 구현
group/infrastructure/persistence/GroupRepositoryImpl.java        ← + 구현
group/infrastructure/persistence/ThemeTagJpaRepository.java      ← + 쿼리 메서드
group/infrastructure/persistence/ThemeTagRepositoryImpl.java     ← + 구현
auth/infrastructure/persistence/MemberRepositoryImpl.java        ← + 구현
```

---

## 실행 단계

- [x] **Step 1** — DTO: `MeetingCardResponse` (inner class `MemberInfo` 포함)
- [x] **Step 2** — Domain: `MeetingRepository` 수정 (+ `findLatestByGroupIdIn`)
- [x] **Step 3** — Domain: `GroupMemberRepository` 수정 (+ `findByMemberId`, `findByGroupIdIn`)
- [x] **Step 4** — Domain: `GroupRepository` 수정 (+ `findAllById`)
- [x] **Step 5** — Domain: `ThemeTagRepository` 수정 (+ `findByCodeIn`)
- [x] **Step 6** — Domain: `MemberRepository` 수정 (+ `findAllById`)
- [x] **Step 7** — Infra: `MeetingJpaRepository` 수정 (JPQL subquery로 그룹당 최신 Meeting)
- [x] **Step 8** — Infra: `MeetingRepositoryImpl` 수정 (+ `findLatestByGroupIdIn` 구현)
- [x] **Step 9** — Infra: `GroupMemberJpaRepository` 수정 (+ `findByMemberId`, `findByGroupIdIn`)
- [x] **Step 10** — Infra: `GroupMemberRepositoryImpl` 수정 (+ 두 메서드 구현)
- [x] **Step 11** — Infra: `GroupRepositoryImpl` 수정 (+ `findAllById` 구현)
- [x] **Step 12** — Infra: `ThemeTagJpaRepository` 수정 (+ `findByCodeIn`)
- [x] **Step 13** — Infra: `ThemeTagRepositoryImpl` 수정 (+ `findByCodeIn` 구현)
- [x] **Step 14** — Infra: `MemberRepositoryImpl` 수정 (+ `findAllById` 구현)
- [x] **Step 15** — Application: `MeetingListService` (신규, 6-step 배치 조회 + 정렬)
- [x] **Step 16** — Presentation: `MeetingController` (신규, `GET /api/v1/meetings`)
- [x] **Step 17** — Review: `aidlc-docs/construction/review/fc6/api.md` (신규)

---

## 주요 설계 결정

| 항목 | 결정 |
|---|---|
| 그룹당 최신 Meeting 조회 | JPQL 서브쿼리 `WHERE id IN (SELECT MAX(id) ... GROUP BY groupId)` |
| N+1 방지 | 6회 고정 배치 쿼리 (루프 안 단건 조회 없음) |
| memberId 추출 | `(Long) auth.getPrincipal()` (기존 컨트롤러 패턴 동일) |
| 정렬 | Java 레벨: listStatus 우선순위 → meeting.createdAt DESC |
| Flyway 마이그레이션 | 불필요 (신규 테이블 없음) |
