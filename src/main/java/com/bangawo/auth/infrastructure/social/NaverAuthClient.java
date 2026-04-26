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
 * 네이버 소셜 인증 클라이언트.
 * iOS가 전달한 Access Token으로 네이버 API를 호출하여 사용자 정보를 조회.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverAuthClient implements SocialAuthClient {

    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";
    private final RestTemplate restTemplate;

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.NAVER;
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
            Map<String, Object> responseData = (Map<String, Object>) body.get("response");

            String socialUserId = (String) responseData.get("id");
            String email = (String) responseData.get("email");

            return new SocialUserInfo(SocialProvider.NAVER, socialUserId, email);
        } catch (Exception e) {
            log.error("네이버 인증 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
    }
}
