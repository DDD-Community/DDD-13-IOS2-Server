package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingTravelBurdenJpaRepository extends JpaRepository<MeetingTravelBurdenJpaEntity, Long> {

    List<MeetingTravelBurdenJpaEntity> findByMeetingId(Long meetingId);

    List<MeetingTravelBurdenJpaEntity> findByMeetingIdAndPlaceId(Long meetingId, Long placeId);

    /** 해당 회원의 이동부담(소요시간·경로 스냅샷)을 물리 삭제 (탈퇴 시 파기 전용) */
    @Modifying
    @Query("DELETE FROM MeetingTravelBurdenJpaEntity b WHERE b.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
