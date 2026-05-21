package com.bangawo.group.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, Long> {
    Optional<GroupMemberJpaEntity> findByGroupIdAndMemberId(Long groupId, Long memberId);
    int countByGroupId(Long groupId);
}
