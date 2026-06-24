package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

import java.util.List;

/**
 * 친구들 거리보기 — 단일 장소에 대한 모임원별 소요시간/환승 (투표 시작 시 저장된 스냅샷 기반).
 */
public record PlaceTravelBurdenResponse(
        PlaceSummary place,
        List<MemberBurden> burdens
) {
    public record MemberBurden(
            Long memberId,
            String name,
            int seconds,
            int transfers,
            boolean isLongest
    ) {}
}
