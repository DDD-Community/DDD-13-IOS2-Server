package com.bangawo.auth.domain;

/** 회원 상태 */
public enum MemberStatus {
    ACTIVE,     // 정상 활동 중
    SUSPENDED,  // 정지 (관리자에 의한 제재 등)
    WITHDRAWN   // 탈퇴
}
