package com.bangawo.group.domain;

import java.util.Optional;

public interface GroupInviteRepository {
    GroupInvite save(GroupInvite invite);
    Optional<GroupInvite> findByCode(String code);
    void deleteByGroupId(Long groupId);
}
