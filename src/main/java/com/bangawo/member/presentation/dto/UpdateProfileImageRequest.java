package com.bangawo.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank String objectKey
) {}
