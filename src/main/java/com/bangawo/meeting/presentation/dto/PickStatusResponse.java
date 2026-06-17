package com.bangawo.meeting.presentation.dto;

import java.util.List;

public record PickStatusResponse(
        List<MemberPickStatus> members,
        List<Long> myPicks
) {}
