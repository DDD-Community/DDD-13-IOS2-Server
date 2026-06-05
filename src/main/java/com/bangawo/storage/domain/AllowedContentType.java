package com.bangawo.storage.domain;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AllowedContentType {
    IMAGE_JPEG("image/jpeg", "jpg"),
    IMAGE_PNG("image/png", "png");

    private final String mimeType;
    private final String extension;

    public static AllowedContentType fromMimeType(String mimeType) {
        return Arrays.stream(values())
                .filter(ct -> ct.mimeType.equals(mimeType))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_INVALID_TYPE));
    }
}
