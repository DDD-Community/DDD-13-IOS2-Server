package com.bangawo.meeting.presentation.dto;

public record MemberPickStatus(
        Long memberId,
        String nickname,
        String profileImageUrl,
        boolean done
) {}
