package com.bangawo.group.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupInviteJpaRepository extends JpaRepository<GroupInviteJpaEntity, Long> {
    Optional<GroupInviteJpaEntity> findByCode(String code);
    void deleteByGroupId(Long groupId);
}
