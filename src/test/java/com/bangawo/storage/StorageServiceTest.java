package com.bangawo.storage;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.storage.application.StorageService;
import com.bangawo.storage.domain.ImageType;
import com.bangawo.storage.infrastructure.gcs.GcsStorageClient;
import com.bangawo.storage.presentation.dto.SignedUrlResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @InjectMocks
    private StorageService storageService;

    @Mock
    private GcsStorageClient gcsStorageClient;

    @Test
    void generateSignedUploadUrl_objectKey_hasCorrectPrefix() {
        Long memberId = 1L;
        given(gcsStorageClient.generateSignedUploadUrl(anyString(), anyString(), any(Duration.class)))
                .willReturn("https://signed-url");

        SignedUrlResponse response = storageService.generateSignedUploadUrl(memberId, ImageType.PROFILE, "image/jpeg");

        assertThat(response.objectKey()).startsWith("profiles/" + memberId + "/");
        assertThat(response.objectKey()).endsWith(".jpg");
        assertThat(response.signedUploadUrl()).isEqualTo("https://signed-url");
    }

    @Test
    void generateSignedUploadUrl_objectKey_containsUUID() {
        Long memberId = 42L;
        given(gcsStorageClient.generateSignedUploadUrl(anyString(), anyString(), any(Duration.class)))
                .willReturn("https://signed-url");

        SignedUrlResponse response = storageService.generateSignedUploadUrl(memberId, ImageType.PROFILE, "image/png");

        String prefix = "profiles/" + memberId + "/";
        String uuidPart = response.objectKey().substring(prefix.length(), response.objectKey().lastIndexOf('.'));
        assertThat(uuidPart).hasSize(36);
    }

    @Test
    void generateSignedUploadUrl_unsupportedContentType_throwsStorageInvalidType() {
        assertThatThrownBy(() -> storageService.generateSignedUploadUrl(1L, ImageType.PROFILE, "image/gif"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORAGE_INVALID_TYPE);
    }

    @Test
    void generateSignedReadUrl_gcsKey_callsGcsClient() {
        String objectKey = "profiles/1/abc.jpg";
        given(gcsStorageClient.generateSignedReadUrl(eq(objectKey), any(Duration.class)))
                .willReturn("https://read-url");

        String result = storageService.generateSignedReadUrl(objectKey);

        assertThat(result).isEqualTo("https://read-url");
        verify(gcsStorageClient).generateSignedReadUrl(eq(objectKey), any(Duration.class));
    }

    @Test
    void generateSignedReadUrl_groupsKey_callsGcsClient() {
        String objectKey = "groups/5/cover.png";
        given(gcsStorageClient.generateSignedReadUrl(eq(objectKey), any(Duration.class)))
                .willReturn("https://read-url");

        String result = storageService.generateSignedReadUrl(objectKey);

        assertThat(result).isEqualTo("https://read-url");
    }

    @Test
    void generateSignedReadUrl_externalUrl_returnedAsIs() {
        String externalUrl = "https://k.kakaocdn.net/profile.jpg";

        String result = storageService.generateSignedReadUrl(externalUrl);

        assertThat(result).isEqualTo(externalUrl);
        verify(gcsStorageClient, never()).generateSignedReadUrl(any(), any());
    }

    @Test
    void generateSignedReadUrl_null_returnedAsNull() {
        String result = storageService.generateSignedReadUrl(null);

        assertThat(result).isNull();
        verify(gcsStorageClient, never()).generateSignedReadUrl(any(), any());
    }
}
