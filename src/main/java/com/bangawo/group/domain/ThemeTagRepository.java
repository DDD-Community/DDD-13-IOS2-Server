package com.bangawo.group.domain;

import java.util.List;

public interface ThemeTagRepository {
    List<ThemeTag> findAllActive();
}
