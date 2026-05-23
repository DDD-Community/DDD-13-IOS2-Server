package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteMethod;
import com.bangawo.meeting.domain.DateVoteSession;
import com.bangawo.meeting.domain.SessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "date_vote_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DateVoteSessionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DateVoteMethod method;

    @Column
    private LocalDate deadline;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static DateVoteSessionJpaEntity from(DateVoteSession session) {
        DateVoteSessionJpaEntity entity = new DateVoteSessionJpaEntity();
        entity.id = session.getId();
        entity.meetingId = session.getMeetingId();
        entity.method = session.getMethod();
        entity.deadline = session.getDeadline();
        entity.durationDays = session.getDurationDays();
        entity.status = session.getStatus();
        entity.createdAt = session.getCreatedAt();
        return entity;
    }

    public DateVoteSession toDomain() {
        return DateVoteSession.builder()
                .id(id)
                .meetingId(meetingId)
                .method(method)
                .deadline(deadline)
                .durationDays(durationDays)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
