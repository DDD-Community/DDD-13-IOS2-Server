package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceVoteSubmitRequest(
        @NotNull @Size(min = 0) List<Long> placeIds
) {}
