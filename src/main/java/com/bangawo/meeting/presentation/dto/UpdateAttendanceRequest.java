package com.bangawo.meeting.presentation.dto;

import com.bangawo.group.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAttendanceRequest(
        @NotNull AttendanceStatus attendanceStatus
) {}
