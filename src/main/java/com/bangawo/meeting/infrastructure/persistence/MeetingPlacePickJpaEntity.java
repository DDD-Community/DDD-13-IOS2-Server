package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlacePick;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meeting_place_pick",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_meeting_member_place",
                columnNames = {"meeting_id", "member_id", "place_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MeetingPlacePickJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "picked_at", nullable = false)
    private LocalDateTime pickedAt;

    public static MeetingPlacePickJpaEntity from(MeetingPlacePick pick) {
        MeetingPlacePickJpaEntity entity = new MeetingPlacePickJpaEntity();
        entity.id = pick.getId();
        entity.meetingId = pick.getMeetingId();
        entity.memberId = pick.getMemberId();
        entity.placeId = pick.getPlaceId();
        entity.pickedAt = pick.getPickedAt();
        return entity;
    }

    public MeetingPlacePick toDomain() {
        return MeetingPlacePick.builder()
                .id(id)
                .meetingId(meetingId)
                .memberId(memberId)
                .placeId(placeId)
                .pickedAt(pickedAt)
                .build();
    }
}
