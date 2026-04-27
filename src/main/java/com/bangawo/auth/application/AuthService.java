package com.bangawo.auth.application;

import com.bangawo.auth.domain.*;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * 인증 서비스.
 * 소셜 로그인 → 회원 조회/생성 → JWT 발급 전체 플로우를 오케스트레이션.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final List<SocialAuthClient> socialAuthClients;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 소셜 로그인.
     * 1) 공급자별 클라이언트로 토큰 검증 → 사용자 정보 획득
     * 2) DB에서 회원 조회, 없으면 신규 생성
     * 3) JWT Access/Refresh Token 발급
     */
    public LoginResult socialLogin(SocialProvider provider, String providerToken) {
        // 해당 공급자의 클라이언트 찾기
        SocialAuthClient client = socialAuthClients.stream()
                .filter(c -> c.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED));

        // 공급자 토큰으로 사용자 정보 조회
        SocialUserInfo userInfo = client.getUserInfo(providerToken);

        // 기존 회원 조회 또는 신규 생성
        boolean isNewMember = false;
        Member member = memberRepository
                .findByProviderAndSocialUserId(provider, userInfo.socialUserId())
                .orElse(null);

        if (member == null) {
            member = Member.create(provider, userInfo.socialUserId(), userInfo.email());
            member = memberRepository.save(member);
            isNewMember = true;
        }

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(member.getId());
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());

        // 기존 Refresh Token 폐기 후 새로 저장
        refreshTokenRepository.revokeAllByMemberId(member.getId());
        saveRefreshToken(member.getId(), refreshToken);

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewMember(isNewMember)
                .registrationCompleted(member.isRegistered())  // DB의 is_registered 컬럼
                .build();
    }

    /**
     * 토큰 갱신.
     * 기존 Refresh Token 검증 → 폐기 → 새 토큰 쌍 발급.
     */
    public LoginResult refreshToken(String refreshToken) {
        // 토큰 자체 유효성 검증 (서명, 만료)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // DB에서 해시로 조회
        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!stored.isValid()) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // 기존 토큰 폐기
        stored.revoke();
        refreshTokenRepository.save(stored);

        // 새 토큰 발급
        Long memberId = jwtProvider.getMemberId(refreshToken);
        String newAccessToken = jwtProvider.generateAccessToken(memberId);
        String newRefreshToken = jwtProvider.generateRefreshToken(memberId);
        saveRefreshToken(memberId, newRefreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .isNewMember(false)
                .registrationCompleted(member.isRegistered())
                .build();
    }

    /** 로그아웃. 해당 회원의 모든 Refresh Token 폐기. */
    public void logout(Long memberId) {
        refreshTokenRepository.revokeAllByMemberId(memberId);
    }

    /** Refresh Token을 SHA-256 해시하여 DB에 저장 */
    private void saveRefreshToken(Long memberId, String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    /** SHA-256 해시 */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** 로그인 결과 DTO (내부용) */
    @lombok.Builder
    @lombok.Getter
    public static class LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final boolean isNewMember;
        private final boolean registrationCompleted;
    }
}
