package com.bangawo.meeting.infrastructure.scheduler;

import com.bangawo.meeting.application.MeetingSchedulerService;
import com.bangawo.meeting.application.PlacePickSchedulerService;
import com.bangawo.meeting.application.PlaceVoteSchedulerService;
import com.bangawo.meeting.application.VoteSchedulerService;
import com.bangawo.meeting.domain.DateVoteSession;
import com.bangawo.meeting.domain.DateVoteSessionRepository;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
    private final PlaceVoteSchedulerService placeVoteSchedulerService;

    @EventListener(ApplicationReadyEvent.class)
    public void processOnStartup() {
        log.info("[서버 기동] 마감이 지났지만 아직 종료 처리 안 된 건들을 먼저 정리합니다.");
        try {
            processScheduled();
            log.info("[서버 기동] 밀린 만료 건 정리 완료. 이후부터는 스케줄러가 매일 자정 자동 처리합니다.");
        } catch (Exception e) {
            log.error("[서버 기동] 밀린 만료 건 정리 중 오류 발생 - 서버 기동은 정상 진행합니다.", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void processScheduled() {
        runStep("(1/4) 날짜 투표 마감 건 자동 확정", this::processExpiredVoteSessions);
        runStep("(2/4) 확정일 지난 모임 자동 종료", this::processExpiredMeetings);
        runStep("(3/4) 장소 담기 마감 건 투표 단계 전환", this::processExpiredPlacePicks);
        runStep("(4/4) 장소 투표 마감 건 종료 및 장소 확정", this::processExpiredPlaceVoteSessions);
        log.info("[만료 처리] 전체 단계 완료");
    }

    private void runStep(String stepName, Runnable step) {
        log.info("[만료 처리] {} 시작", stepName);
        try {
            step.run();
            log.info("[만료 처리] {} 완료", stepName);
        } catch (Exception e) {
            log.error("[만료 처리] {} 단계 실패 - 다음 단계는 계속 진행합니다.", stepName, e);
        }
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

    private void processExpiredPlaceVoteSessions() {
        try {
            placeVoteSchedulerService.closeExpiredSessions();
        } catch (Exception e) {
            log.error("투표 마감 CONFIRMED 전환 일괄 처리 실패", e);
        }
    }
}
