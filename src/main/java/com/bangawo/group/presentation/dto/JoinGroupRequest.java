package com.bangawo.group.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGroupRequest(@NotBlank String inviteCode) {}
