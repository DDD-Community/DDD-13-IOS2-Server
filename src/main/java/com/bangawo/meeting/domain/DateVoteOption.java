package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DateVoteOption {

    private Long id;
    private Long sessionId;
    private LocalDateTime candidateDate;
    private int sortOrder;

    @Builder
    public DateVoteOption(Long id, Long sessionId, LocalDateTime candidateDate, int sortOrder) {
        this.id = id;
        this.sessionId = sessionId;
        this.candidateDate = candidateDate;
        this.sortOrder = sortOrder;
    }
}
