package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MeetingTravelBurden {

    private Long id;
    private Long meetingId;
    private Long memberId;
    private Long placeId;
    private int seconds;
    private int transfers;
    private List<TravelPathPoint> stationPath;

    @Builder
    public MeetingTravelBurden(Long id, Long meetingId, Long memberId,
                                Long placeId, int seconds, int transfers,
                                List<TravelPathPoint> stationPath) {
        this.id = id;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.placeId = placeId;
        this.seconds = seconds;
        this.transfers = transfers;
        this.stationPath = stationPath != null ? stationPath : List.of();
    }

    public static MeetingTravelBurden of(Long meetingId, Long memberId, Long placeId,
                                          int seconds, int transfers,
                                          List<TravelPathPoint> stationPath) {
        return MeetingTravelBurden.builder()
                .meetingId(meetingId)
                .memberId(memberId)
                .placeId(placeId)
                .seconds(seconds)
                .transfers(transfers)
                .stationPath(stationPath)
                .build();
    }
}
