package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmDateRequest(
        @NotNull Long optionId
) {}
