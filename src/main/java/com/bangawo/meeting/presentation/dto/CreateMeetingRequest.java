package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMeetingRequest(
        @NotBlank @Size(max = 30) String name,
        @NotBlank String themeTagCode,
        List<String> categoryLabels,
        List<String> vibes,
        Boolean reservable,
        Boolean parking
) {}
