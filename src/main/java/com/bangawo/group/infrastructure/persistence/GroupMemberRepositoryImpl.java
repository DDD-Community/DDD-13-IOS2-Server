package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepositoryImpl implements GroupMemberRepository {

    private final GroupMemberJpaRepository jpaRepository;

    @Override
    public GroupMember save(GroupMember groupMember) {
        return jpaRepository.save(GroupMemberJpaEntity.from(groupMember)).toDomain();
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId) {
        return jpaRepository.findByGroupIdAndMemberId(groupId, memberId)
                .map(GroupMemberJpaEntity::toDomain);
    }

    @Override
    public int countByGroupId(Long groupId) {
        return jpaRepository.countByGroupId(groupId);
    }
}
