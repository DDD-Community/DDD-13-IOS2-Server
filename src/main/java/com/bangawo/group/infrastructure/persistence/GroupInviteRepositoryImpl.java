package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.GroupInvite;
import com.bangawo.group.domain.GroupInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupInviteRepositoryImpl implements GroupInviteRepository {

    private final GroupInviteJpaRepository jpaRepository;

    @Override
    public GroupInvite save(GroupInvite invite) {
        return jpaRepository.save(GroupInviteJpaEntity.from(invite)).toDomain();
    }

    @Override
    public Optional<GroupInvite> findByCode(String code) {
        return jpaRepository.findByCode(code).map(GroupInviteJpaEntity::toDomain);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        jpaRepository.deleteByGroupId(groupId);
    }
}
