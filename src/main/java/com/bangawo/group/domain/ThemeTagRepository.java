package com.bangawo.group.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ThemeTagRepository {
    List<ThemeTag> findAllActive();
    List<ThemeTag> findByCodeIn(Set<String> codes);
    Optional<ThemeTag> findByCode(String code);
}
