package com.bangawo.storage.infrastructure.gcs;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
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

    /** 객체 삭제 (best-effort). 실패해도 예외를 던지지 않고 false를 반환한다. */
    public boolean delete(String objectKey) {
        try {
            return storage.delete(BlobId.of(bucketName, objectKey));
        } catch (Exception e) {
            log.warn("GCS 객체 삭제 실패: objectKey={}", objectKey, e);
            return false;
        }
    }
}
