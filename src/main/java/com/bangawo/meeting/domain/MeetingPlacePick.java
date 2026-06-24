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
    private PickSource source;

    @Builder
    public MeetingPlacePick(Long id, Long meetingId, Long memberId, Long placeId,
                            LocalDateTime pickedAt, PickSource source) {
        this.id = id;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.placeId = placeId;
        this.pickedAt = pickedAt;
        this.source = source;
    }

    public static MeetingPlacePick of(Long meetingId, Long memberId, Long placeId) {
        return MeetingPlacePick.builder()
                .meetingId(meetingId)
                .memberId(memberId)
                .placeId(placeId)
                .pickedAt(LocalDateTime.now())
                .source(PickSource.USER)
                .build();
    }

    public static MeetingPlacePick ofSystem(Long meetingId, Long placeId) {
        return MeetingPlacePick.builder()
                .meetingId(meetingId)
                .memberId(null)
                .placeId(placeId)
                .pickedAt(LocalDateTime.now())
                .source(PickSource.SYSTEM)
                .build();
    }
}
