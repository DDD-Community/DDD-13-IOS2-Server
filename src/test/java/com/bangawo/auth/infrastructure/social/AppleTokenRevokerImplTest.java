package com.bangawo.auth.infrastructure.social;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppleTokenRevokerImplTest {

    @Mock
    AppleRevokeProperties properties;

    @InjectMocks
    AppleTokenRevokerImpl appleTokenRevoker;

    @Test
    void 자격증명이_미설정이면_예외없이_false를_반환한다() {
        when(properties.isConfigured()).thenReturn(false);

        boolean result = appleTokenRevoker.revoke("some-authorization-code");

        assertThat(result).isFalse();
    }
}
