package com.bangawo.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() throws Exception {
        jwtProvider = new JwtProvider();
        setField(jwtProvider, "secretKeyString",
                Base64.getEncoder().encodeToString(new byte[64]));
        setField(jwtProvider, "accessTokenExpiration", 3600000L);
        setField(jwtProvider, "refreshTokenExpiration", 2592000000L);
        jwtProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성 및 검증")
    void generateAndValidateAccessToken() {
        String token = jwtProvider.generateAccessToken(1L);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Refresh Token 생성 및 검증")
    void generateAndValidateRefreshToken() {
        String token = jwtProvider.generateRefreshToken(1L);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("잘못된 토큰은 검증 실패")
    void invalidTokenReturnsFalse() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰은 검증 실패")
    void emptyTokenReturnsFalse() {
        assertThat(jwtProvider.validateToken("")).isFalse();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
