package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MidpointStationCandidate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "midpoint_station_candidate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MidpointStationCandidateJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(nullable = false)
    private int rank;

    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(nullable = false)
    private String lines;

    @Column(name = "distance_km", nullable = false, precision = 6, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public static MidpointStationCandidateJpaEntity from(MidpointStationCandidate domain) {
        MidpointStationCandidateJpaEntity e = new MidpointStationCandidateJpaEntity();
        e.id = domain.getId();
        e.meetingId = domain.getMeetingId();
        e.rank = domain.getRank();
        e.stationId = domain.getStationId();
        e.stationName = domain.getStationName();
        e.lines = domain.getLines();
        e.distanceKm = BigDecimal.valueOf(domain.getDistanceKm());
        e.latitude = domain.getLatitude();
        e.longitude = domain.getLongitude();
        return e;
    }

    public MidpointStationCandidate toDomain() {
        return MidpointStationCandidate.builder()
                .id(id)
                .meetingId(meetingId)
                .rank(rank)
                .stationId(stationId)
                .stationName(stationName)
                .lines(lines)
                .distanceKm(distanceKm.doubleValue())
                .latitude(latitude != null ? latitude : 0.0)
                .longitude(longitude != null ? longitude : 0.0)
                .build();
    }
}
