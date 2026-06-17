package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.LocationStatus;
import com.bangawo.meeting.domain.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingJpaRepository extends JpaRepository<MeetingJpaEntity, Long> {

    @Query("SELECT m FROM MeetingJpaEntity m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM MeetingJpaEntity m2 WHERE m2.groupId IN :groupIds GROUP BY m2.groupId)")
    List<MeetingJpaEntity> findLatestByGroupIdIn(@Param("groupIds") List<Long> groupIds);

    @Query("SELECT m FROM MeetingJpaEntity m WHERE m.groupId = :groupId ORDER BY m.id DESC LIMIT 1")
    Optional<MeetingJpaEntity> findLatestByGroupId(@Param("groupId") Long groupId);

    List<MeetingJpaEntity> findByStatusAndConfirmedDateBefore(MeetingStatus status, LocalDate today);

    List<MeetingJpaEntity> findByLocationStatusAndPickDeadlineBefore(LocationStatus locationStatus, LocalDateTime now);
}
