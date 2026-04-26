package com.bangawo.auth.presentation.dto;

/** 로그인/토큰갱신 응답 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isNewMember,            // 최초 소셜 로그인 여부
        boolean registrationCompleted   // 회원가입 완료 여부 (닉네임 + 약관 동의 완료)
) {}
