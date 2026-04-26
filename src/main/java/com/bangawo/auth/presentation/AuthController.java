package com.bangawo.auth.presentation;

import com.bangawo.auth.application.AuthService;
import com.bangawo.auth.presentation.dto.LoginRequest;
import com.bangawo.auth.presentation.dto.LoginResponse;
import com.bangawo.auth.presentation.dto.RefreshRequest;
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

@Tag(name = "인증", description = "소셜 로그인, 토큰 갱신, 로그아웃")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "iOS에서 받은 소셜 토큰으로 로그인. 신규 회원이면 isNewMember=true")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.socialLogin(
                request.getProvider(), request.getProviderToken());

        return ResponseEntity.ok(new LoginResponse(
                result.getAccessToken(), result.getRefreshToken(),
                result.isNewMember(), result.isRegistrationCompleted()));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access/Refresh Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthService.LoginResult result = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(new LoginResponse(
                result.getAccessToken(), result.getRefreshToken(),
                false, result.isRegistrationCompleted()));
    }

    @Operation(summary = "로그아웃", description = "해당 회원의 모든 Refresh Token 폐기")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);
        return ResponseEntity.ok().build();
    }
}
