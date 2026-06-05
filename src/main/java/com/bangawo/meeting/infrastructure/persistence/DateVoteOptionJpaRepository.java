package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DateVoteOptionJpaRepository extends JpaRepository<DateVoteOptionJpaEntity, Long> {
    List<DateVoteOptionJpaEntity> findBySessionId(Long sessionId);
}
