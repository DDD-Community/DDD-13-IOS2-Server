package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetingConfirmedPlace {

    private Long id;
    private Long meetingId;
    private Long placeId;
    private String placeName;
    private String address;
    private LocalDateTime confirmedAt;

    @Builder
    public MeetingConfirmedPlace(Long id, Long meetingId, Long placeId,
                                  String placeName, String address, LocalDateTime confirmedAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.placeId = placeId;
        this.placeName = placeName;
        this.address = address;
        this.confirmedAt = confirmedAt;
    }

    public static MeetingConfirmedPlace of(Long meetingId, Long placeId,
                                            String placeName, String address) {
        return MeetingConfirmedPlace.builder()
                .meetingId(meetingId)
                .placeId(placeId)
                .placeName(placeName)
                .address(address)
                .confirmedAt(LocalDateTime.now())
                .build();
    }
}
