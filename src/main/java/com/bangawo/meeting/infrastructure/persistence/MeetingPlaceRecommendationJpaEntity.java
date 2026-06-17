package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceRecommendation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_place_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingPlaceRecommendationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false)
    private int rank;

    @Column(nullable = false)
    private double score;

    @Column(name = "nearest_station_id", nullable = false)
    private Long nearestStationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static MeetingPlaceRecommendationJpaEntity from(MeetingPlaceRecommendation domain) {
        MeetingPlaceRecommendationJpaEntity e = new MeetingPlaceRecommendationJpaEntity();
        e.id = domain.getId();
        e.meetingId = domain.getMeetingId();
        e.placeId = domain.getPlaceId();
        e.rank = domain.getRank();
        e.score = domain.getScore();
        e.nearestStationId = domain.getNearestStationId();
        e.createdAt = domain.getCreatedAt();
        return e;
    }

    public MeetingPlaceRecommendation toDomain() {
        return MeetingPlaceRecommendation.builder()
                .id(id)
                .meetingId(meetingId)
                .placeId(placeId)
                .rank(rank)
                .score(score)
                .nearestStationId(nearestStationId)
                .createdAt(createdAt)
                .build();
    }
}
