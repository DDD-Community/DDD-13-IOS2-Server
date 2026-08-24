package com.bangawo.meeting.domain;

import java.util.List;

import java.util.Optional;

public interface MeetingParticipantRepository {
    MeetingParticipant save(MeetingParticipant participant);
    void saveAll(List<MeetingParticipant> participants);
    Optional<MeetingParticipant> findByMeetingIdAndMemberId(Long meetingId, Long memberId);
    List<MeetingParticipant> findByMeetingId(Long meetingId);
    List<MeetingParticipant> findByMeetingIdIn(List<Long> meetingIds);
    boolean existsByMeetingId(Long meetingId);
    /** 해당 회원이 참여 중인 모든 모임 참여 행 조회 (탈퇴 시 출발지 파기 대상 조회용) */
    List<MeetingParticipant> findByMemberId(Long memberId);
}
