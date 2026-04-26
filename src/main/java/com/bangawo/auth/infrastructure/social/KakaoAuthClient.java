package com.bangawo.auth.infrastructure.social;

import com.bangawo.auth.application.SocialAuthClient;
import com.bangawo.auth.application.SocialUserInfo;
import com.bangawo.auth.domain.SocialProvider;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 소셜 인증 클라이언트.
 * iOS가 전달한 Access Token으로 카카오 API를 호출하여 사용자 정보를 조회.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthClient implements SocialAuthClient {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final RestTemplate restTemplate;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SocialUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, request, Map.class);

            Map<String, Object> body = response.getBody();
            String socialUserId = String.valueOf(body.get("id"));

            // 이메일은 kakao_account 안에 있음 (동의 항목에 따라 없을 수 있음)
            String email = null;
            Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
            if (account != null && account.get("email") != null) {
                email = (String) account.get("email");
            }

            return new SocialUserInfo(SocialProvider.KAKAO, socialUserId, email);
        } catch (Exception e) {
            log.error("카카오 인증 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
    }
}
