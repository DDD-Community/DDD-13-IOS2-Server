package com.bangawo.member.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/** 회원가입 요청 */
@Getter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    private String nickname;

    @NotEmpty(message = "약관 동의 목록은 필수입니다")
    private List<Long> agreedTermsIds;

    @NotBlank(message = "출발지 라벨은 필수입니다")
    private String departureLabel;

    @NotBlank(message = "출발지 주소는 필수입니다")
    private String departureAddress;

    @NotBlank(message = "출발지 도로명 주소는 필수입니다")
    private String departureRoadAddress;

    private String departurePlaceName;

    private Boolean departureIsDefault;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
}
