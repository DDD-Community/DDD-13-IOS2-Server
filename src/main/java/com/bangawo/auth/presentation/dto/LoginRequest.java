package com.bangawo.auth.presentation.dto;

import com.bangawo.auth.domain.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 소셜 로그인 요청 */
@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "provider는 필수입니다")
    private SocialProvider provider;  // KAKAO, NAVER, APPLE

    @NotBlank(message = "providerToken은 필수입니다")
    private String providerToken;     // iOS에서 받은 소셜 토큰
}
