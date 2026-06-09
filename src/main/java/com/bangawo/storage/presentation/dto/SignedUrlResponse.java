package com.bangawo.storage.presentation.dto;

public record SignedUrlResponse(
        String signedUploadUrl,
        String objectKey
) {}
