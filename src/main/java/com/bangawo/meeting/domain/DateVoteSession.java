package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class DateVoteSession {

    private Long id;
    private Long meetingId;
    private DateVoteMethod method;
    private LocalDate deadline;
    private Integer durationDays;
    private SessionStatus status;
    private LocalDateTime createdAt;

    @Builder
    public DateVoteSession(Long id, Long meetingId, DateVoteMethod method,
                           LocalDate deadline, Integer durationDays,
                           SessionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.method = method;
        this.deadline = deadline;
        this.durationDays = durationDays;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static DateVoteSession ofHostPick(Long meetingId) {
        return DateVoteSession.builder()
                .meetingId(meetingId)
                .method(DateVoteMethod.HOST_PICK)
                .status(SessionStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static DateVoteSession ofVote(Long meetingId, int durationDays) {
        return DateVoteSession.builder()
                .meetingId(meetingId)
                .method(DateVoteMethod.VOTE)
                .deadline(LocalDate.now().plusDays(durationDays))
                .durationDays(durationDays)
                .status(SessionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void confirm() {
        this.status = SessionStatus.CONFIRMED;
    }

    public void expire() {
        this.status = SessionStatus.EXPIRED;
    }
}
