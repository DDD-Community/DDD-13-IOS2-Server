package com.bangawo.member.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.member.domain.member.NicknameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 오케스트레이션.
 * 닉네임 검증 → 약관 동의 → 프로필 저장 → 기본 출발지 등록.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final NicknameValidator nicknameValidator;
    private final TermsService termsService;
    private final DeparturePlaceService departurePlaceService;

    /** 닉네임 금칙어 검증 (입력 화면에서 사전 검증용) */
    public void validateNickname(String nickname) {
        nicknameValidator.validate(nickname);
    }

    /** 회원가입 (소셜 로그인 후 프로필 + 약관 + 출발지 한 번에 처리) */
    public Member register(Long memberId, String nickname, java.util.List<Long> agreedTermsIds,
                           String departureLabel, String departureAddress,
                           String departureRoadAddress, String departurePlaceName,
                           double latitude, double longitude, boolean departureIsDefault) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 가입 완료된 회원
        if (member.isRegistered())
            throw new BusinessException(ErrorCode.INVALID_INPUT);

        // 닉네임 금칙어 검증 (검증 API와 이중 검증 — 방어적 처리)
        nicknameValidator.validate(nickname);

        // 약관 동의 처리 + 필수 약관 검증
        termsService.agreeTerms(memberId, agreedTermsIds);
        termsService.validateRequiredTermsAgreed(memberId);

        // 기본 출발지 등록 (닉네임보다 먼저 — 출발지 실패 시 닉네임 저장 방지)
        departurePlaceService.create(memberId, departureLabel, departureAddress,
                departureRoadAddress, departurePlaceName, latitude, longitude, departureIsDefault);

        // 프로필 업데이트 + 회원가입 완료 처리
        member.updateProfile(nickname, null);
        member.completeRegistration();
        member = memberRepository.save(member);

        return member;
    }

    /** 프로필 조회 */
    @Transactional(readOnly = true)
    public Member getProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /** 프로필 이미지 변경 */
    public Member updateProfileImage(Long memberId, String objectKey) {
        if (!objectKey.startsWith("profiles/") && !objectKey.startsWith("groups/")) {
            throw new BusinessException(ErrorCode.STORAGE_INVALID_PATH);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfileImageUrl(objectKey);
        return memberRepository.save(member);
    }

    /** 닉네임 변경 */
    public void updateNickname(Long memberId, String nickname) {
        nicknameValidator.validate(nickname);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfile(nickname, member.getProfileImageUrl());
        memberRepository.save(member);
    }
}
