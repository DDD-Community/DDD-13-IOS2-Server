package com.bangawo.meeting.infrastructure.scheduler;

import com.bangawo.meeting.application.VoteSchedulerService;
import com.bangawo.meeting.domain.DateVoteSession;
import com.bangawo.meeting.domain.DateVoteSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingScheduler {

    private final DateVoteSessionRepository dateVoteSessionRepository;
    private final VoteSchedulerService voteSchedulerService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void processScheduled() {
        List<DateVoteSession> expiredSessions =
                dateVoteSessionRepository.findActiveByDeadlineBefore(LocalDate.now());

        for (DateVoteSession session : expiredSessions) {
            try {
                voteSchedulerService.processExpiredSession(session);
            } catch (Exception e) {
                log.error("투표 자동 확정 실패 sessionId={}", session.getId(), e);
            }
        }
    }
}
