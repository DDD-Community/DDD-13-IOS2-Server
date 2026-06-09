package com.bangawo.storage.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    PROFILE("profiles/"),
    GROUP("groups/");

    private final String folderPrefix;
}
