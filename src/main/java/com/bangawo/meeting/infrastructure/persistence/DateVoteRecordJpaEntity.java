package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteRecord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "date_vote_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DateVoteRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_id", nullable = false)
    private Long optionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    public static DateVoteRecordJpaEntity from(DateVoteRecord record) {
        DateVoteRecordJpaEntity entity = new DateVoteRecordJpaEntity();
        entity.id = record.getId();
        entity.optionId = record.getOptionId();
        entity.memberId = record.getMemberId();
        entity.votedAt = record.getVotedAt();
        return entity;
    }

    public DateVoteRecord toDomain() {
        return DateVoteRecord.builder()
                .id(id)
                .optionId(optionId)
                .memberId(memberId)
                .votedAt(votedAt)
                .build();
    }
}
