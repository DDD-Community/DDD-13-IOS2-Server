package com.bangawo.group.domain;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository {
    GroupMember save(GroupMember groupMember);
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);
    int countByGroupId(Long groupId);
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByMemberId(Long memberId);
    List<GroupMember> findByGroupIdIn(List<Long> groupIds);
}
