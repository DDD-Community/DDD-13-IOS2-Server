package com.bangawo.meeting.application;

import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacePickSchedulerService {

    private final MeetingRepository meetingRepository;

    @Transactional
    public void processExpiredPickDeadlines() {
        List<Meeting> meetings = meetingRepository.findRecommendedWithExpiredPickDeadline(LocalDateTime.now());
        for (Meeting meeting : meetings) {
            try {
                meeting.toVoting();
                meetingRepository.save(meeting);
                log.info("담기 마감 VOTING 전환 meetingId={}", meeting.getId());
            } catch (Exception e) {
                log.error("담기 마감 VOTING 전환 실패 meetingId={}", meeting.getId(), e);
            }
        }
    }
}
