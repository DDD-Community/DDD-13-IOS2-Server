package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceVoteSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_place_vote_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingPlaceVoteSessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false, length = 12)
    private String status;

    public static MeetingPlaceVoteSessionJpaEntity from(MeetingPlaceVoteSession session) {
        MeetingPlaceVoteSessionJpaEntity entity = new MeetingPlaceVoteSessionJpaEntity();
        entity.id = session.getId();
        entity.meetingId = session.getMeetingId();
        entity.startedAt = session.getStartedAt();
        entity.deadline = session.getDeadline();
        entity.status = session.getStatus();
        return entity;
    }

    public MeetingPlaceVoteSession toDomain() {
        return MeetingPlaceVoteSession.builder()
                .id(id)
                .meetingId(meetingId)
                .startedAt(startedAt)
                .deadline(deadline)
                .status(status)
                .build();
    }
}
