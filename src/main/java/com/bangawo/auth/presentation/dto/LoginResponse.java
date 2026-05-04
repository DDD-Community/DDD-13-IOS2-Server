package com.bangawo.auth.presentation.dto;

/** 로그인/토큰갱신 응답 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean firstSocialLogin,       // 이번 요청에서 회원이 새로 생성됐는지 (첫 소셜 로그인)
        boolean registrationCompleted   // 회원가입 완료 여부 (닉네임 + 약관 동의 완료)
) {}
