package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteOption;
import com.bangawo.meeting.domain.DateVoteOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DateVoteOptionRepositoryImpl implements DateVoteOptionRepository {

    private final DateVoteOptionJpaRepository jpaRepository;

    @Override
    public List<DateVoteOption> saveAll(List<DateVoteOption> options) {
        return jpaRepository.saveAll(options.stream().map(DateVoteOptionJpaEntity::from).toList())
                .stream()
                .map(DateVoteOptionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<DateVoteOption> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId)
                .stream()
                .map(DateVoteOptionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<DateVoteOption> findById(Long id) {
        return jpaRepository.findById(id).map(DateVoteOptionJpaEntity::toDomain);
    }
}
