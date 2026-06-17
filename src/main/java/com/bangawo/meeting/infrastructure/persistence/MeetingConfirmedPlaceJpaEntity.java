package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingConfirmedPlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_confirmed_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingConfirmedPlaceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    public static MeetingConfirmedPlaceJpaEntity from(MeetingConfirmedPlace confirmed) {
        MeetingConfirmedPlaceJpaEntity entity = new MeetingConfirmedPlaceJpaEntity();
        entity.id = confirmed.getId();
        entity.meetingId = confirmed.getMeetingId();
        entity.placeId = confirmed.getPlaceId();
        entity.placeName = confirmed.getPlaceName();
        entity.address = confirmed.getAddress();
        entity.confirmedAt = confirmed.getConfirmedAt();
        return entity;
    }

    public MeetingConfirmedPlace toDomain() {
        return MeetingConfirmedPlace.builder()
                .id(id).meetingId(meetingId).placeId(placeId)
                .placeName(placeName).address(address).confirmedAt(confirmedAt)
                .build();
    }
}
