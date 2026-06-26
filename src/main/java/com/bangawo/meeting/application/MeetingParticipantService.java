package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingParticipantService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    @Transactional
    public void updateAttendance(Long meetingId, Long memberId, AttendanceStatus status) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        MeetingParticipant participant = meetingParticipantRepository
                .findByMeetingIdAndMemberId(meetingId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND));

        participant.updateAttendance(status.name());
        meetingParticipantRepository.save(participant);
    }
}
