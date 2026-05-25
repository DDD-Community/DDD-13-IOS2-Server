package com.bangawo.meeting.application;

import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingSchedulerService {

    private final MeetingRepository meetingRepository;

    @Transactional
    public void closeMeeting(Meeting meeting) {
        meeting.close();
        meetingRepository.save(meeting);
        log.info("모임 자동 종료 완료 meetingId={}", meeting.getId());
    }
}
