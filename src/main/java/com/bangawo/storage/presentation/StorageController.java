package com.bangawo.storage.presentation;

import com.bangawo.storage.application.StorageService;
import com.bangawo.storage.presentation.dto.SignedUrlRequest;
import com.bangawo.storage.presentation.dto.SignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스토리지", description = "이미지 업로드 Signed URL 발급")
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "이미지 업로드 Signed URL 발급", description = "GCS PUT Signed URL 및 objectKey 반환 (15분 유효)")
    @PostMapping("/images")
    public ResponseEntity<SignedUrlResponse> getSignedUploadUrl(Authentication auth,
                                                                 @Valid @RequestBody SignedUrlRequest req) {
        Long memberId = (Long) auth.getPrincipal();
        SignedUrlResponse response = storageService.generateSignedUploadUrl(memberId, req.imageType(), req.contentType());
        return ResponseEntity.ok(response);
    }
}
