package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.MeetingPlaceVote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meeting_place_vote",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_session_member_place",
                columnNames = {"session_id", "member_id", "place_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingPlaceVoteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    public static MeetingPlaceVoteJpaEntity from(MeetingPlaceVote vote) {
        MeetingPlaceVoteJpaEntity entity = new MeetingPlaceVoteJpaEntity();
        entity.id = vote.getId();
        entity.sessionId = vote.getSessionId();
        entity.memberId = vote.getMemberId();
        entity.placeId = vote.getPlaceId();
        entity.votedAt = vote.getVotedAt();
        return entity;
    }

    public MeetingPlaceVote toDomain() {
        return MeetingPlaceVote.builder()
                .id(id)
                .sessionId(sessionId)
                .memberId(memberId)
                .placeId(placeId)
                .votedAt(votedAt)
                .build();
    }
}
