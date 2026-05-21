# Business Overview

## Business Description

- **서비스명**: 반가워 (Bangawo)
- **핵심 목적**: iOS 앱 사용자들이 모임 날짜·장소를 함께 결정할 수 있는 모임 조율 서비스의 백엔드 API
- **현재 구현 범위**: 소셜 로그인(카카오/네이버/애플), 회원 프로필 관리, 출발지 등록
- **MVP1 목표**: 그룹 생성·초대, 모임 생성, 날짜 투표, 생명주기 관리

## Business Transactions (현재 구현)

| 트랜잭션 | 컨텍스트 | 설명 |
|---|---|---|
| 소셜 로그인 | auth | 소셜 토큰 → 회원 조회/생성 → JWT 발급 |
| 토큰 갱신 | auth | Refresh Token → 새 Access/Refresh 발급 |
| 로그아웃 | auth | Refresh Token 전체 폐기 |
| 회원가입 완료 | member | 닉네임 설정 + 약관 동의 |
| 출발지 관리 | member | 출발지 등록/조회/삭제 (최대 10개) |
| 약관 조회 | member | 필수/선택 약관 목록 조회 |

## Business Dictionary

| 용어 | 내부 구조 | 설명 |
|---|---|---|
| 모임 | Group(1):Meeting(N) | 사용자에게는 "모임"으로 노출, 내부는 그룹+모임 2계층 |
| 호스트 | Group 생성자 | 모임 생성·투표 시작·확정·종료 권한 보유 |
| 구성원 | Membership | 초대 링크로 합류한 사람 |
| 출발지 | DeparturePlace | 회원이 등록한 출발 위치 (좌표+주소) |
