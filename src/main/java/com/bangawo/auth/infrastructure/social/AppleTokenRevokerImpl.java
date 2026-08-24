package com.bangawo.auth.infrastructure.social;

import com.bangawo.auth.application.AppleTokenRevoker;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Apple Sign in with Apple REST API를 통한 연동 해제(revoke) 구현체.
 * client_secret(ES256 서명 JWT) 생성 → authorization code를 refresh token으로 교환 → revoke 호출.
 * 자격증명 미설정 시 no-op(false 반환)으로 동작하여 개발·테스트 환경에서 탈퇴 기능을 막지 않는다.
 *
 * 근거: App Store Review Guideline 5.1.1(v) — 계정 삭제 시 Apple 토큰 revoke 의무.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleTokenRevokerImpl implements AppleTokenRevoker {

    private static final String TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    private static final String AUDIENCE = "https://appleid.apple.com";
    private static final Duration CLIENT_SECRET_TTL = Duration.ofMinutes(5);

    private final AppleRevokeProperties properties;

    private final RestClient restClient = RestClient.create();

    @Override
    public boolean revoke(String authorizationCode) {
        if (!properties.isConfigured()) {
            log.info("Apple revoke skip — 자격증명 미설정");
            return false;
        }
        try {
            String clientSecret = generateClientSecret();
            String refreshToken = exchangeForRefreshToken(authorizationCode, clientSecret);
            revokeRefreshToken(refreshToken, clientSecret);
            return true;
        } catch (Exception e) {
            log.warn("Apple revoke 실패", e);
            return false;
        }
    }

    private String generateClientSecret() throws Exception {
        PrivateKey privateKey = loadPrivateKey(properties.getPrivateKey());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + CLIENT_SECRET_TTL.toMillis());

        return Jwts.builder()
                .header().keyId(properties.getKeyId()).and()
                .issuer(properties.getTeamId())
                .subject(properties.getClientId())
                .audience().add(AUDIENCE).and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }

    @SuppressWarnings("unchecked")
    private String exchangeForRefreshToken(String authorizationCode, String clientSecret) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", clientSecret);

        Map<String, Object> response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        Object refreshToken = response != null ? response.get("refresh_token") : null;
        if (refreshToken == null) {
            throw new IllegalStateException("Apple token 교환 실패: refresh_token 미수신");
        }
        return refreshToken.toString();
    }

    private void revokeRefreshToken(String refreshToken, String clientSecret) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", clientSecret);

        restClient.post()
                .uri(REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
