package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingTravelBurden;
import com.bangawo.meeting.domain.TravelPathPoint;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(
        name = "meeting_travel_burden",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_burden_meeting_member_place",
                columnNames = {"meeting_id", "member_id", "place_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingTravelBurdenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private int seconds;

    @Column(nullable = false)
    private int transfers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "station_path", columnDefinition = "jsonb")
    private List<TravelPathPoint> stationPath;

    public static MeetingTravelBurdenJpaEntity from(MeetingTravelBurden burden) {
        MeetingTravelBurdenJpaEntity entity = new MeetingTravelBurdenJpaEntity();
        entity.id = burden.getId();
        entity.meetingId = burden.getMeetingId();
        entity.memberId = burden.getMemberId();
        entity.placeId = burden.getPlaceId();
        entity.seconds = burden.getSeconds();
        entity.transfers = burden.getTransfers();
        entity.stationPath = burden.getStationPath();
        return entity;
    }

    public MeetingTravelBurden toDomain() {
        return MeetingTravelBurden.builder()
                .id(id)
                .meetingId(meetingId)
                .memberId(memberId)
                .placeId(placeId)
                .seconds(seconds)
                .transfers(transfers)
                .stationPath(stationPath)
                .build();
    }
}
