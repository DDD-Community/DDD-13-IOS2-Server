package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetingPlaceRecommendation {

    private Long id;
    private Long meetingId;
    private Long placeId;
    private int rank;
    private double score;
    private Long nearestStationId;
    private LocalDateTime createdAt;

    @Builder
    public MeetingPlaceRecommendation(Long id, Long meetingId, Long placeId, int rank,
                                       double score, Long nearestStationId, LocalDateTime createdAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.placeId = placeId;
        this.rank = rank;
        this.score = score;
        this.nearestStationId = nearestStationId;
        this.createdAt = createdAt;
    }

    public static MeetingPlaceRecommendation of(Long meetingId, Long placeId, int rank,
                                                 double score, Long nearestStationId) {
        return MeetingPlaceRecommendation.builder()
                .meetingId(meetingId)
                .placeId(placeId)
                .rank(rank)
                .score(score)
                .nearestStationId(nearestStationId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
