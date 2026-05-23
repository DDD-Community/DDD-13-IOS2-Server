package com.bangawo.group.domain;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findById(Long id);
    List<Group> findAllById(List<Long> ids);
}
