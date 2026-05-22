package com.bangawo.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingJpaRepository extends JpaRepository<MeetingJpaEntity, Long> {

    @Query("SELECT m FROM MeetingJpaEntity m WHERE m.id IN " +
           "(SELECT MAX(m2.id) FROM MeetingJpaEntity m2 WHERE m2.groupId IN :groupIds GROUP BY m2.groupId)")
    List<MeetingJpaEntity> findLatestByGroupIdIn(@Param("groupIds") List<Long> groupIds);
}
