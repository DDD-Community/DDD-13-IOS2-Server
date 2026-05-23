package com.bangawo.meeting.domain;

import java.util.List;

public interface DateVoteRecordRepository {
    List<DateVoteRecord> saveAll(List<DateVoteRecord> records);
    List<DateVoteRecord> findByOptionIdIn(List<Long> optionIds);
    void deleteByOptionIdInAndMemberId(List<Long> optionIds, Long memberId);
    long countDistinctMemberIdByOptionIdIn(List<Long> optionIds);
}
