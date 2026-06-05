package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MeetingParticipant {

    private Long id;
    private Long meetingId;
    private Long memberId;
    private Double latitude;
    private Double longitude;
    private String attendanceStatus;

    @Builder
    public MeetingParticipant(Long id, Long meetingId, Long memberId,
                               Double latitude, Double longitude, String attendanceStatus) {
        this.id = id;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.attendanceStatus = attendanceStatus;
    }

    public static MeetingParticipant create(Long meetingId, Long memberId,
                                             Double latitude, Double longitude,
                                             String attendanceStatus) {
        return MeetingParticipant.builder()
                .meetingId(meetingId).memberId(memberId)
                .latitude(latitude).longitude(longitude)
                .attendanceStatus(attendanceStatus)
                .build();
    }

    public boolean hasCoordinate() {
        return latitude != null && longitude != null;
    }

    public void updateDeparture(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
