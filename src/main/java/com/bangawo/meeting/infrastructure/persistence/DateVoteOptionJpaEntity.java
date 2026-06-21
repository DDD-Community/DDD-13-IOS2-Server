package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "date_vote_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DateVoteOptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "candidate_date", nullable = false)
    private LocalDateTime candidateDate;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static DateVoteOptionJpaEntity from(DateVoteOption option) {
        DateVoteOptionJpaEntity entity = new DateVoteOptionJpaEntity();
        entity.id = option.getId();
        entity.sessionId = option.getSessionId();
        entity.candidateDate = option.getCandidateDate();
        entity.sortOrder = option.getSortOrder();
        return entity;
    }

    public DateVoteOption toDomain() {
        return DateVoteOption.builder()
                .id(id)
                .sessionId(sessionId)
                .candidateDate(candidateDate)
                .sortOrder(sortOrder)
                .build();
    }
}
