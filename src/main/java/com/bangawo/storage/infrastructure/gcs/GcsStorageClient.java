package com.bangawo.storage.infrastructure.gcs;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GcsStorageClient {

    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    public String generateSignedUploadUrl(String objectKey, String contentType, Duration ttl) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectKey)
                    .setContentType(contentType)
                    .build();
            return storage.signUrl(
                    blobInfo,
                    ttl.toMinutes(),
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withV4Signature()
            ).toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.STORAGE_SIGNED_URL_FAILED);
        }
    }

    public String generateSignedReadUrl(String objectKey, Duration ttl) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectKey).build();
            return storage.signUrl(
                    blobInfo,
                    ttl.toHours(),
                    TimeUnit.HOURS,
                    Storage.SignUrlOption.withV4Signature()
            ).toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.STORAGE_SIGNED_URL_FAILED);
        }
    }
}
