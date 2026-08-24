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
    private String departureLabel;
    private String departurePlaceName;
    private String departureAddress;

    @Builder
    public MeetingParticipant(Long id, Long meetingId, Long memberId,
                               Double latitude, Double longitude, String attendanceStatus,
                               String departureLabel, String departurePlaceName, String departureAddress) {
        this.id = id;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.attendanceStatus = attendanceStatus;
        this.departureLabel = departureLabel;
        this.departurePlaceName = departurePlaceName;
        this.departureAddress = departureAddress;
    }

    public static MeetingParticipant create(Long meetingId, Long memberId,
                                             Double latitude, Double longitude,
                                             String attendanceStatus,
                                             String departureLabel, String departurePlaceName,
                                             String departureAddress) {
        return MeetingParticipant.builder()
                .meetingId(meetingId).memberId(memberId)
                .latitude(latitude).longitude(longitude)
                .attendanceStatus(attendanceStatus)
                .departureLabel(departureLabel)
                .departurePlaceName(departurePlaceName)
                .departureAddress(departureAddress)
                .build();
    }

    public boolean hasCoordinate() {
        return latitude != null && longitude != null;
    }

    public void updateAttendance(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public void updateDeparture(double latitude, double longitude,
                                String departureLabel, String departurePlaceName, String departureAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.departureLabel = departureLabel;
        this.departurePlaceName = departurePlaceName;
        this.departureAddress = departureAddress;
    }

    /** 출발지 표시명: 카카오 장소명 우선, 없으면 사용자 별칭, 둘 다 없으면 null */
    public String departureName() {
        return departurePlaceName != null ? departurePlaceName : departureLabel;
    }

    /** 출발지 정보 파기 — 좌표 및 출발지 메타를 모두 제거 (참여 이력은 유지) */
    public void clearDeparture() {
        this.latitude = null;
        this.longitude = null;
        this.departureLabel = null;
        this.departurePlaceName = null;
        this.departureAddress = null;
    }
}
