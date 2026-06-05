# 비즈니스 규칙 — FC-7-1 내 정보 수정

## 참석여부 수정 규칙

### 변경 규칙
- 요청자 본인의 참석여부만 수정 가능하다
- `attendance_status`는 그룹 단위로 관리된다 (meeting과 무관)
- 유효한 값: `JOIN` (참여) / `LATE` (늦참) / `ABSENT` (불참)

### 인가 규칙
- JWT 인증 필수
- JWT의 memberId가 해당 groupId의 group_member로 등록되어 있어야 한다
- 타인의 참석여부는 수정 불가

### 제약사항

| 항목 | 제약 | 위반 시 |
|---|---|---|
| JWT | 필수 | 401 AUTH_001 |
| 그룹 멤버 여부 | group_member 레코드 존재 필수 | 403 GROUP_003 |
| attendanceStatus | JOIN / LATE / ABSENT | 400 COMMON_001 |

---

## 출발지 추가 규칙

### 생성 규칙
- 회원당 최대 3개까지 등록 가능하다
- 출발지가 0개인 상태에서 추가 시 isDefault가 강제로 true로 설정된다
- isDefault=true 요청 시 기존 기본 출발지를 자동으로 해제한다

### 인가 규칙
- JWT 인증 필수
- 본인의 출발지만 추가 가능 (JWT memberId 기준)

### 제약사항

| 항목 | 제약 | 위반 시 |
|---|---|---|
| JWT | 필수 | 401 AUTH_001 |
| 최대 개수 | 3개 | 400 MEMBER_003 |
| label | 필수, 최대 10자 | 400 COMMON_001 |
| address | 필수 (지번 주소) | 400 COMMON_001 |
| roadAddress | 필수 (도로명 주소) | 400 COMMON_001 |
| placeName | 선택 (nullable) | - |
| latitude / longitude | 필수, 유효한 좌표 | 400 COMMON_001 |

---

## 출발지 수정 규칙

### 변경 규칙
- 변경 가능 필드: label, address, roadAddress, placeName, latitude, longitude
- isDefault는 수정 불가 (장소 정보만 변경)
- 본인 소유 출발지만 수정 가능

### 인가 규칙
- JWT 인증 필수
- `findByIdAndMemberId`로 본인 소유 검증 — 타인 소유 또는 존재하지 않으면 404 반환

### 예외 케이스

| 상황 | 처리 |
|---|---|
| 타인 소유 출발지 수정 시도 | 404 MEMBER_005 (존재 여부 노출 방지) |
| 기본 출발지의 장소 정보 수정 | 허용 (isDefault 상태는 유지) |
