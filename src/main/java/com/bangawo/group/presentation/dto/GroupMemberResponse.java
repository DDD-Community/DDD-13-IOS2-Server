package com.bangawo.group.presentation.dto;

import java.time.LocalDateTime;

public record GroupMemberResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        String role,
        LocalDateTime joinedAt
) {}
