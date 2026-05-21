package com.bangawo.group.domain;

import java.util.Optional;

public interface GroupMemberRepository {
    GroupMember save(GroupMember groupMember);
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);
    int countByGroupId(Long groupId);
}
