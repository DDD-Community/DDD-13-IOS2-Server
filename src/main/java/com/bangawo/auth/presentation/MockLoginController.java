package com.bangawo.auth.presentation;

import com.bangawo.global.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[LOCAL] Mock 인증", description = "로컬 개발 전용 — 소셜 로그인 없이 JWT 발급")
@Profile("local")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MockLoginController {

    private final JwtProvider jwtProvider;

    @Operation(
            summary = "[LOCAL ONLY] Mock 로그인 — memberId로 JWT 즉시 발급",
            description = "DB에 존재하는 memberId를 입력하면 Access Token을 반환합니다. local 프로파일에서만 동작."
    )
    @PostMapping("/mock-login")
    public MockLoginResponse mockLogin(@RequestParam Long memberId) {
        String accessToken = jwtProvider.generateAccessToken(memberId);
        return new MockLoginResponse(accessToken);
    }

    public record MockLoginResponse(String accessToken) {}
}
