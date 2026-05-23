package com.bangawo.group.presentation.dto;

import com.bangawo.group.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceUpdateRequest(
        @NotNull AttendanceStatus attendanceStatus
) {}
