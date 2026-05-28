package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MidpointStationCandidate {

    private Long id;
    private Long meetingId;
    private int rank;
    private String stationName;
    private String lines;
    private double distanceKm;

    @Builder
    public MidpointStationCandidate(Long id, Long meetingId, int rank,
                                     String stationName, String lines, double distanceKm) {
        this.id = id;
        this.meetingId = meetingId;
        this.rank = rank;
        this.stationName = stationName;
        this.lines = lines;
        this.distanceKm = distanceKm;
    }

    public static MidpointStationCandidate of(Long meetingId, int rank,
                                               String stationName, String lines, double distanceKm) {
        return MidpointStationCandidate.builder()
                .meetingId(meetingId)
                .rank(rank)
                .stationName(stationName)
                .lines(lines)
                .distanceKm(distanceKm)
                .build();
    }
}
