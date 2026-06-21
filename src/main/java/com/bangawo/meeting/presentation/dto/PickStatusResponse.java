package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

import java.util.List;

public record PickStatusResponse(
        List<MemberPickStatus> members,
        List<PlaceSummary> myPicks
) {}
