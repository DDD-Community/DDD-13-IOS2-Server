package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record StartVoteRequest(
        @NotEmpty @Size(min = 1, max = 3) List<@NotNull LocalDate> candidateDates,
        @NotNull Integer durationDays
) {}
