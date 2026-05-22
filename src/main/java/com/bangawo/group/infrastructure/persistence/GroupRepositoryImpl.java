package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupJpaRepository jpaRepository;

    @Override
    public Group save(Group group) {
        return jpaRepository.save(GroupJpaEntity.from(group)).toDomain();
    }

    @Override
    public Optional<Group> findById(Long id) {
        return jpaRepository.findById(id).map(GroupJpaEntity::toDomain);
    }

    @Override
    public List<Group> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(GroupJpaEntity::toDomain)
                .toList();
    }
}
