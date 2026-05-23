package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DateVoteRecord {

    private Long id;
    private Long optionId;
    private Long memberId;
    private LocalDateTime votedAt;

    @Builder
    public DateVoteRecord(Long id, Long optionId, Long memberId, LocalDateTime votedAt) {
        this.id = id;
        this.optionId = optionId;
        this.memberId = memberId;
        this.votedAt = votedAt;
    }

    public static DateVoteRecord of(Long optionId, Long memberId) {
        return DateVoteRecord.builder()
                .optionId(optionId)
                .memberId(memberId)
                .votedAt(LocalDateTime.now())
                .build();
    }
}
