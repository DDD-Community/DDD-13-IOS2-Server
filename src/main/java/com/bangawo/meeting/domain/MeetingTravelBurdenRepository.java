package com.bangawo.meeting.domain;

import java.util.List;

public interface MeetingTravelBurdenRepository {
    void saveAll(List<MeetingTravelBurden> burdens);
    List<MeetingTravelBurden> findByMeetingId(Long meetingId);
    List<MeetingTravelBurden> findByMeetingIdAndPlaceId(Long meetingId, Long placeId);
    /** 해당 회원의 이동부담(소요시간·경로 스냅샷)을 물리 삭제 (탈퇴 시 파기 전용) */
    void deleteAllByMemberId(Long memberId);
}
