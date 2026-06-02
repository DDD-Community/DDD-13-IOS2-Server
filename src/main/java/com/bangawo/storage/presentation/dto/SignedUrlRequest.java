package com.bangawo.storage.presentation.dto;

import com.bangawo.storage.domain.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignedUrlRequest(
        @NotNull ImageType imageType,
        @NotBlank String contentType
) {}
