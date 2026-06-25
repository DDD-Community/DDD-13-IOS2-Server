package com.bangawo.meeting.presentation.dto;

import com.bangawo.place.presentation.dto.PlaceSummary;

import java.util.List;

/**
 * 친구들 거리보기 — 단일 장소에 대한 모임 활성 참여자 전원의 소요시간/환승/경로 (투표 시작 시 저장된 스냅샷 기반).
 */
public record PlaceTravelBurdenResponse(
        PlaceSummary place,
        List<MemberBurden> burdens
) {
    public record MemberBurden(
            Long memberId,
            String name,
            String departureName,
            boolean isMe,
            Integer seconds,
            Integer transfers,
            boolean isLongest,
            List<PathPoint> path
    ) {}

    /** 이동 경로의 한 지점 (지하철 역). 출발역→도착역 순서. */
    public record PathPoint(
            Long stationId,
            double latitude,
            double longitude
    ) {}
}
