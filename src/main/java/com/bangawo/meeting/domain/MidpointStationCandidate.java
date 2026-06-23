package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MidpointStationCandidate {

    private Long id;
    private Long meetingId;
    private int rank;
    private Long stationId;
    private String stationName;
    private String lines;
    private double distanceKm;
    private double latitude;
    private double longitude;

    @Builder
    public MidpointStationCandidate(Long id, Long meetingId, int rank, Long stationId,
                                     String stationName, String lines, double distanceKm,
                                     double latitude, double longitude) {
        this.id = id;
        this.meetingId = meetingId;
        this.rank = rank;
        this.stationId = stationId;
        this.stationName = stationName;
        this.lines = lines;
        this.distanceKm = distanceKm;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static MidpointStationCandidate of(Long meetingId, int rank, Long stationId,
                                               String stationName, String lines, double distanceKm,
                                               double latitude, double longitude) {
        return MidpointStationCandidate.builder()
                .meetingId(meetingId)
                .rank(rank)
                .stationId(stationId)
                .stationName(stationName)
                .lines(lines)
                .distanceKm(distanceKm)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
