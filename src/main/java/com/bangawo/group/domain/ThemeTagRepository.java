package com.bangawo.group.domain;

import java.util.List;
import java.util.Set;

public interface ThemeTagRepository {
    List<ThemeTag> findAllActive();
    List<ThemeTag> findByCodeIn(Set<String> codes);
}
