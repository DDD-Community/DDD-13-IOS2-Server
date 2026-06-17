package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetingPlacePick {

    private Long id;
    private Long meetingId;
    private Long memberId;
    private Long placeId;
    private LocalDateTime pickedAt;

    @Builder
    public MeetingPlacePick(Long id, Long meetingId, Long memberId, Long placeId, LocalDateTime pickedAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.placeId = placeId;
        this.pickedAt = pickedAt;
    }

    public static MeetingPlacePick of(Long meetingId, Long memberId, Long placeId) {
        return MeetingPlacePick.builder()
                .meetingId(meetingId)
                .memberId(memberId)
                .placeId(placeId)
                .pickedAt(LocalDateTime.now())
                .build();
    }
}
