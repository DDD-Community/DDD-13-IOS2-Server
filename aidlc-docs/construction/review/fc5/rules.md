# 비즈니스 규칙 — FC-5 구성원 초대 및 합류

## 초대 코드 규칙

- 호스트만 발급 가능
- 발급 시 기존 코드 무효화 (그룹당 1개 유지)
- 유효기간: 발급 후 48시간
- 코드 형식: UUID (36자)

## 합류 규칙

- 회원가입(기본 출발지 등록) 미완료(`is_registered=false`) 회원은 합류 불가 → 403
- 만료된 코드로 합류 시도 → 400
- 이미 그룹 멤버인 상태에서 합류 시도 → 400
- 합류 시 role = MEMBER, attendanceStatus = JOIN으로 자동 설정
- 합류 시 활성 모임(CLOSED 아닌)이 있으면 meeting_participant 자동 생성
  - 가입 완료 ⟺ 기본 출발지 보유가 동치이므로 lat/lng는 항상 채워져서 생성됨 (null 삽입 불가)
- 합류 시 활성 모임이 없으면 meeting_participant 생성 안 함

## 제약사항

| 항목 | 제약 | 위반 시 |
|---|---|---|
| 가입 완료 | member.is_registered = true | 403 MEMBER_006 |
| 초대 코드 존재 | group_invite 조회 결과 있어야 함 | 404 GROUP_006 |
| 초대 코드 만료 | expires_at > now | 400 GROUP_007 |
| 중복 합류 | group_member 미존재 | 400 GROUP_008 |
