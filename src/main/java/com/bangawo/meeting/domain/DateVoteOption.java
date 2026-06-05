package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateVoteOption {

    private Long id;
    private Long sessionId;
    private LocalDate candidateDate;
    private int sortOrder;

    @Builder
    public DateVoteOption(Long id, Long sessionId, LocalDate candidateDate, int sortOrder) {
        this.id = id;
        this.sessionId = sessionId;
        this.candidateDate = candidateDate;
        this.sortOrder = sortOrder;
    }
}
