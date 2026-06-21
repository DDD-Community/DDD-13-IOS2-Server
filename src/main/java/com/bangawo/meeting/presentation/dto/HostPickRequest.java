package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HostPickRequest(
        @NotNull @Future LocalDateTime date
) {}
