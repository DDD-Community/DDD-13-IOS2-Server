# Business Rules — Unit 1 (FC-4)

## 생성 규칙

### BR-1: 이름 길이 제한
- 그룹명(= 모임명)은 최대 **30자**
- 위반 시: `BusinessException(ErrorCode.GROUP_NAME_TOO_LONG)` → HTTP 400

### BR-2: 그룹명 = 모임명
- 입력받은 name 하나로 Group.name 과 Meeting.name 동일하게 저장
- 별도 처리 없음 — 동일 값 그대로 사용

### BR-3: 생성자 자동 HOST 부여
- 그룹 생성 즉시 GroupMember(role=HOST, attendanceStatus=JOIN) 자동 생성
- 별도 API 호출 없이 그룹 생성 트랜잭션 내 처리

### BR-4: 그룹과 첫 모임은 반드시 함께 생성
- Group만 단독으로 존재할 수 없음
- 그룹 생성 실패 시 모임도 생성되지 않음 (단일 트랜잭션 롤백)

### BR-5: 첫 모임의 초기 상태
- locationStatus = BEFORE (선정 전)
- dateVoteStatus = BEFORE (선정 전)
- confirmedDate = null

## 인가 규칙 (Security Baseline)

### BR-6: 인증된 사용자만 그룹 생성 가능
- 미인증 요청 → HTTP 401

## 데이터 제약

| 항목 | 제약 |
|---|---|
| 그룹명 | 1~30자 |
| 테마 태그 | 8개 enum 값 중 하나 (필수) |
| 최대 구성원 | 20명 (생성 시는 해당 없음, 초대 시 검증) |
