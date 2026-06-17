package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetingPlaceVote {

    private Long id;
    private Long sessionId;
    private Long memberId;
    private Long placeId;
    private LocalDateTime votedAt;

    @Builder
    public MeetingPlaceVote(Long id, Long sessionId, Long memberId, Long placeId, LocalDateTime votedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.placeId = placeId;
        this.votedAt = votedAt;
    }

    public static MeetingPlaceVote of(Long sessionId, Long memberId, Long placeId) {
        return MeetingPlaceVote.builder()
                .sessionId(sessionId)
                .memberId(memberId)
                .placeId(placeId)
                .votedAt(LocalDateTime.now())
                .build();
    }
}
