package com.bangawo.meeting.presentation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HostPickRequest(
        @NotNull @Future LocalDate date
) {}
