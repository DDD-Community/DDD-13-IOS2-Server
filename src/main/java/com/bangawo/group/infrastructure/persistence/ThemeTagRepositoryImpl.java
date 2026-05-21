package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.ThemeTag;
import com.bangawo.group.domain.ThemeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ThemeTagRepositoryImpl implements ThemeTagRepository {

    private final ThemeTagJpaRepository themeTagJpaRepository;

    @Override
    public List<ThemeTag> findAllActive() {
        return themeTagJpaRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(e -> ThemeTag.builder()
                        .id(e.getId())
                        .code(e.getCode())
                        .displayName(e.getDisplayName())
                        .sortOrder(e.getSortOrder())
                        .active(e.isActive())
                        .build())
                .toList();
    }
}
