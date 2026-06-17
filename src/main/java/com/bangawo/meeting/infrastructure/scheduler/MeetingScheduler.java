package com.bangawo.meeting.infrastructure.scheduler;

import com.bangawo.meeting.application.MeetingSchedulerService;
import com.bangawo.meeting.application.PlacePickSchedulerService;
import com.bangawo.meeting.application.VoteSchedulerService;
import com.bangawo.meeting.domain.DateVoteSession;
import com.bangawo.meeting.domain.DateVoteSessionRepository;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
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
    private final MeetingRepository meetingRepository;
    private final VoteSchedulerService voteSchedulerService;
    private final MeetingSchedulerService meetingSchedulerService;
    private final PlacePickSchedulerService placePickSchedulerService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void processScheduled() {
        processExpiredVoteSessions();
        processExpiredMeetings();
        processExpiredPlacePicks();
    }

    private void processExpiredVoteSessions() {
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

    private void processExpiredMeetings() {
        List<Meeting> expiredMeetings =
                meetingRepository.findActiveByConfirmedDateBefore(LocalDate.now());

        for (Meeting meeting : expiredMeetings) {
            try {
                meetingSchedulerService.closeMeeting(meeting);
            } catch (Exception e) {
                log.error("모임 자동 종료 실패 meetingId={}", meeting.getId(), e);
            }
        }
    }

    private void processExpiredPlacePicks() {
        try {
            placePickSchedulerService.processExpiredPickDeadlines();
        } catch (Exception e) {
            log.error("담기 마감 VOTING 전환 일괄 처리 실패", e);
        }
    }
}
