package com.bangawo.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 토큰 갱신 요청 */
@Getter
@NoArgsConstructor
public class RefreshRequest {

    @NotBlank(message = "refreshToken은 필수입니다")
    private String refreshToken;
}
