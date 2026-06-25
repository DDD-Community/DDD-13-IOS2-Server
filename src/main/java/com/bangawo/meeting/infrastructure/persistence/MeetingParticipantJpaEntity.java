package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingParticipant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingParticipantJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "attendance_status", nullable = false, length = 10)
    private String attendanceStatus;

    @Column(name = "departure_label", length = 20)
    private String departureLabel;

    @Column(name = "departure_place_name", length = 100)
    private String departurePlaceName;

    @Column(name = "departure_address", length = 255)
    private String departureAddress;

    public static MeetingParticipantJpaEntity from(MeetingParticipant domain) {
        MeetingParticipantJpaEntity e = new MeetingParticipantJpaEntity();
        e.id = domain.getId();
        e.meetingId = domain.getMeetingId();
        e.memberId = domain.getMemberId();
        e.latitude = domain.getLatitude();
        e.longitude = domain.getLongitude();
        e.attendanceStatus = domain.getAttendanceStatus();
        e.departureLabel = domain.getDepartureLabel();
        e.departurePlaceName = domain.getDeparturePlaceName();
        e.departureAddress = domain.getDepartureAddress();
        return e;
    }

    public MeetingParticipant toDomain() {
        return MeetingParticipant.builder()
                .id(id).meetingId(meetingId).memberId(memberId)
                .latitude(latitude).longitude(longitude)
                .attendanceStatus(attendanceStatus)
                .departureLabel(departureLabel)
                .departurePlaceName(departurePlaceName)
                .departureAddress(departureAddress)
                .build();
    }
}
