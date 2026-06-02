package com.bangawo.member.presentation;

import com.bangawo.auth.domain.Member;
import com.bangawo.member.application.MemberService;
import com.bangawo.member.presentation.dto.MemberResponse;
import com.bangawo.member.presentation.dto.RegisterRequest;
import com.bangawo.member.presentation.dto.UpdateProfileImageRequest;
import com.bangawo.storage.application.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원가입, 프로필 조회/수정")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final StorageService storageService;

    @Operation(summary = "닉네임 금칙어 검증", description = "닉네임 입력 화면에서 사용. 통과 시 200, 금칙어 시 400")
    @PostMapping("/nickname/validate")
    public ResponseEntity<Void> validateNickname(@RequestBody java.util.Map<String, String> body) {
        memberService.validateNickname(body.get("nickname"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입", description = "닉네임 + 약관 동의 + 기본 출발지 등록")
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(Authentication auth,
                                                    @Valid @RequestBody RegisterRequest req) {
        Long memberId = (Long) auth.getPrincipal();
        Member member = memberService.register(memberId, req.getNickname(),
                req.getAgreedTermsIds(), req.getDepartureLabel(), req.getDepartureAddress(),
                req.getLatitude(), req.getLongitude());
        String resolvedUrl = storageService.generateSignedReadUrl(member.getProfileImageUrl());
        return ResponseEntity.ok(MemberResponse.from(member, resolvedUrl));
    }

    @Operation(summary = "프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getProfile(Authentication auth) {
        Long memberId = (Long) auth.getPrincipal();
        Member member = memberService.getProfile(memberId);
        String resolvedUrl = storageService.generateSignedReadUrl(member.getProfileImageUrl());
        return ResponseEntity.ok(MemberResponse.from(member, resolvedUrl));
    }

    @Operation(summary = "프로필 이미지 변경")
    @PatchMapping("/me/profile-image")
    public ResponseEntity<MemberResponse> updateProfileImage(Authentication auth,
                                                              @Valid @RequestBody UpdateProfileImageRequest req) {
        Long memberId = (Long) auth.getPrincipal();
        Member member = memberService.updateProfileImage(memberId, req.objectKey());
        String resolvedUrl = storageService.generateSignedReadUrl(member.getProfileImageUrl());
        return ResponseEntity.ok(MemberResponse.from(member, resolvedUrl));
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/me/nickname")
    public ResponseEntity<Void> updateNickname(Authentication auth,
                                                @RequestBody java.util.Map<String, String> body) {
        Long memberId = (Long) auth.getPrincipal();
        memberService.updateNickname(memberId, body.get("nickname"));
        return ResponseEntity.ok().build();
    }
}
