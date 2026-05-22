package com.bangawo.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeparturePlaceRequest(
        @NotBlank @Size(max = 10) String label,
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull Boolean isDefault
) {}
