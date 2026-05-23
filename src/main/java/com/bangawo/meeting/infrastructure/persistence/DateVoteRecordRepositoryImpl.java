package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteRecord;
import com.bangawo.meeting.domain.DateVoteRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DateVoteRecordRepositoryImpl implements DateVoteRecordRepository {

    private final DateVoteRecordJpaRepository jpaRepository;

    @Override
    public List<DateVoteRecord> saveAll(List<DateVoteRecord> records) {
        return jpaRepository.saveAll(records.stream().map(DateVoteRecordJpaEntity::from).toList())
                .stream()
                .map(DateVoteRecordJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<DateVoteRecord> findByOptionIdIn(List<Long> optionIds) {
        return jpaRepository.findByOptionIdIn(optionIds)
                .stream()
                .map(DateVoteRecordJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteByOptionIdInAndMemberId(List<Long> optionIds, Long memberId) {
        jpaRepository.deleteByOptionIdInAndMemberId(optionIds, memberId);
    }

    @Override
    public long countDistinctMemberIdByOptionIdIn(List<Long> optionIds) {
        return jpaRepository.countDistinctMemberIdByOptionIdIn(optionIds);
    }
}
