package com.bangawo.auth.infrastructure.social;

import com.bangawo.auth.application.SocialAuthClient;
import com.bangawo.auth.application.SocialUserInfo;
import com.bangawo.auth.domain.SocialProvider;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * 애플 소셜 인증 클라이언트.
 * iOS가 전달한 ID Token(JWT)을 디코딩하여 사용자 정보를 추출.
 * 애플은 Access Token이 아닌 ID Token을 사용하므로 별도 API 호출 없이 토큰 자체에서 정보를 꺼냄.
 *
 * TODO: 프로덕션에서는 Apple 공개키(JWKS)로 서명 검증 필요
 *       https://appleid.apple.com/auth/keys
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAuthClient implements SocialAuthClient {

    private final ObjectMapper objectMapper;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo getUserInfo(String idToken) {
        try {
            // ID Token의 payload(두 번째 파트)를 Base64 디코딩하여 사용자 정보 추출
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("잘못된 ID Token 형식");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // sub = 애플이 부여한 사용자 고유 ID (변하지 않음)
            String socialUserId = (String) claims.get("sub");
            String email = (String) claims.get("email"); // 최초 로그인 시에만 포함될 수 있음

            return new SocialUserInfo(SocialProvider.APPLE, socialUserId, email);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("애플 인증 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
    }
}
