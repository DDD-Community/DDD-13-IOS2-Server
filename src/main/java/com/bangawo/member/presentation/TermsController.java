package com.bangawo.member.presentation;

import com.bangawo.member.application.TermsService;
import com.bangawo.member.presentation.dto.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "약관", description = "약관 조회")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "현재 약관 목록 조회", description = "회원가입 시 표시할 약관 목록")
    @GetMapping
    public ResponseEntity<List<TermsResponse>> getCurrentTerms() {
        return ResponseEntity.ok(
                termsService.getCurrentTerms().stream()
                        .map(TermsResponse::from).toList());
    }
}
