package com.bangawo.auth.domain;

/**
 * 소셜 로그인 공급자.
 * 각 공급자마다 토큰 검증 방식이 다름:
 * - KAKAO, NAVER: Access Token → 공급자 API 호출로 사용자 정보 조회
 * - APPLE: ID Token(JWT) → 토큰 자체를 검증하여 사용자 정보 추출
 */
public enum SocialProvider {
    KAKAO,
    NAVER,
    APPLE
}
