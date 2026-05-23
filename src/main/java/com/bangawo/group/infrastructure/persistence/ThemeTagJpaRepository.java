package com.bangawo.group.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ThemeTagJpaRepository extends JpaRepository<ThemeTagJpaEntity, Long> {
    List<ThemeTagJpaEntity> findByActiveTrueOrderBySortOrderAsc();
    List<ThemeTagJpaEntity> findByCodeIn(Set<String> codes);
}
