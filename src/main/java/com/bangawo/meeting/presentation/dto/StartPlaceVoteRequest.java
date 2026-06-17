package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record StartPlaceVoteRequest(
        @NotNull Integer durationDays
) {}
