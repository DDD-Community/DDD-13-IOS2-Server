package com.bangawo.meeting.domain;

import java.util.List;
import java.util.Optional;

public interface DateVoteOptionRepository {
    List<DateVoteOption> saveAll(List<DateVoteOption> options);
    List<DateVoteOption> findBySessionId(Long sessionId);
    Optional<DateVoteOption> findById(Long id);
}
