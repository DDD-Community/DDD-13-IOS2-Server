package com.bangawo.group.infrastructure.persistence;

import com.bangawo.group.domain.ThemeTag;
import com.bangawo.group.domain.ThemeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ThemeTagRepositoryImpl implements ThemeTagRepository {

    private final ThemeTagJpaRepository themeTagJpaRepository;

    @Override
    public List<ThemeTag> findAllActive() {
        return themeTagJpaRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ThemeTag> findByCodeIn(Set<String> codes) {
        return themeTagJpaRepository.findByCodeIn(codes).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ThemeTag> findByCode(String code) {
        return themeTagJpaRepository.findByCode(code).map(this::toDomain);
    }

    private ThemeTag toDomain(ThemeTagJpaEntity e) {
        return ThemeTag.builder()
                .id(e.getId())
                .code(e.getCode())
                .displayName(e.getDisplayName())
                .sortOrder(e.getSortOrder())
                .active(e.isActive())
                .build();
    }
}
