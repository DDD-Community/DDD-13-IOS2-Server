package com.bangawo.meeting.application;

import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingPlaceVoteSession;
import com.bangawo.meeting.domain.MeetingPlaceVoteSessionRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceVoteSchedulerService {

    private final MeetingPlaceVoteSessionRepository voteSessionRepository;
    private final MeetingRepository meetingRepository;
    private final PlaceConfirmService placeConfirmService;

    @Transactional
    public void closeExpiredSessions() {
        List<MeetingPlaceVoteSession> expired = voteSessionRepository.findInProgressWithExpiredDeadline();
        for (MeetingPlaceVoteSession session : expired) {
            try {
                session.close();
                voteSessionRepository.save(session);

                Meeting meeting = meetingRepository.findById(session.getMeetingId()).orElse(null);
                if (meeting != null) {
                    meeting.toConfirmed();
                    meetingRepository.save(meeting);
                    placeConfirmService.confirmPlace(session.getMeetingId());
                    log.info("투표 마감 CONFIRMED 전환 meetingId={}", session.getMeetingId());
                }
            } catch (Exception e) {
                log.error("투표 마감 처리 실패 sessionId={}", session.getId(), e);
            }
        }
    }
}
