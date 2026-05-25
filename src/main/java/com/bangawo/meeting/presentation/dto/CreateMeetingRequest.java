package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMeetingRequest(
        @NotBlank @Size(max = 30) String name,
        @NotBlank String themeTagCode
) {}
