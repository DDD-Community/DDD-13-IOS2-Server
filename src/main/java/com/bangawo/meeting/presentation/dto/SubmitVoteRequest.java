package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitVoteRequest(
        @NotEmpty List<Long> optionIds
) {}
