package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class MeetingPlaceVoteSession {

    private Long id;
    private Long meetingId;
    private LocalDateTime startedAt;
    private LocalDateTime deadline;
    private String status;

    @Builder
    public MeetingPlaceVoteSession(Long id, Long meetingId, LocalDateTime startedAt,
                                    LocalDateTime deadline, String status) {
        this.id = id;
        this.meetingId = meetingId;
        this.startedAt = startedAt;
        this.deadline = deadline;
        this.status = status;
    }

    public static MeetingPlaceVoteSession create(Long meetingId, int durationDays) {
        LocalDateTime deadline = LocalDate.now().plusDays(durationDays)
                .atTime(23, 59, 59);
        return MeetingPlaceVoteSession.builder()
                .meetingId(meetingId)
                .startedAt(LocalDateTime.now())
                .deadline(deadline)
                .status("IN_PROGRESS")
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    public void close() {
        this.status = "CLOSED";
    }
}
