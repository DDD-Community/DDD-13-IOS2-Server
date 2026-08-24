package com.bangawo.storage.application;

import com.bangawo.storage.domain.AllowedContentType;
import com.bangawo.storage.domain.ImageType;
import com.bangawo.storage.infrastructure.gcs.GcsStorageClient;
import com.bangawo.storage.presentation.dto.SignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Duration UPLOAD_TTL = Duration.ofMinutes(15);
    private static final Duration READ_TTL = Duration.ofHours(1);

    private final GcsStorageClient gcsStorageClient;

    public SignedUrlResponse generateSignedUploadUrl(Long memberId, ImageType imageType, String contentType) {
        AllowedContentType allowedType = AllowedContentType.fromMimeType(contentType);
        String objectKey = imageType.getFolderPrefix() + memberId + "/" + UUID.randomUUID() + "." + allowedType.getExtension();
        String signedUrl = gcsStorageClient.generateSignedUploadUrl(objectKey, contentType, UPLOAD_TTL);
        return new SignedUrlResponse(signedUrl, objectKey);
    }

    public String generateSignedReadUrl(String objectKey) {
        if (objectKey == null || objectKey.startsWith("http")) {
            return objectKey;
        }
        if (objectKey.startsWith("profiles/") || objectKey.startsWith("groups/")) {
            return gcsStorageClient.generateSignedReadUrl(objectKey, READ_TTL);
        }
        return objectKey;
    }

    /** 프로필 이미지 객체 삭제 (best-effort). profiles/ 접두사가 아니면 삭제하지 않는다. */
    public boolean delete(String objectKey) {
        if (objectKey == null || !objectKey.startsWith("profiles/")) {
            return false;
        }
        return gcsStorageClient.delete(objectKey);
    }
}
