package com.bangawo.auth.application;

import com.bangawo.auth.domain.SocialProvider;

/**
 * 소셜 인증 클라이언트 인터페이스.
 * 공급자별로 구현체가 다름 (카카오/네이버: Access Token, 애플: ID Token).
 */
public interface SocialAuthClient {
    SocialProvider getProvider();
    SocialUserInfo getUserInfo(String providerToken);
}
