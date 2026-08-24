package com.bangawo.member.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.auth.domain.RefreshTokenRepository;
import com.bangawo.auth.domain.SocialProvider;
import com.bangawo.auth.application.AppleTokenRevoker;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingTravelBurdenRepository;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import com.bangawo.member.domain.terms.TermsAgreementRepository;
import com.bangawo.storage.application.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 회원 탈퇴 오케스트레이션.
 * 파기 대상(개인정보 처리방침 근거)이 여러 컨텍스트에 흩어져 있으므로,
 * 파기 누락 방지를 위해 리포지토리를 직접 주입해 한 서비스에서 전체 흐름을 관리한다.
 *
 * 트랜잭션 경계: 호스트 승계 → 참여 데이터 파기 → 개인 소유 데이터 파기 → 회원 익명화는 단일 트랜잭션.
 * GCS 이미지 삭제·Apple revoke는 트랜잭션 밖에서 best-effort로 수행한다(실패해도 탈퇴는 유지).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingTravelBurdenRepository meetingTravelBurdenRepository;
    private final StorageService storageService;
    private final AppleTokenRevoker appleTokenRevoker;
    private final TransactionTemplate transactionTemplate;

    /**
     * 회원 탈퇴.
     * @param memberId 탈퇴 대상 (인증 principal)
     * @param appleAuthorizationCode Apple 연동 해제용 코드 (nullable)
     */
    public void withdraw(Long memberId, String appleAuthorizationCode) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.isWithdrawn()) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }

        String profileObjectKey = member.getProfileImageUrl();
        SocialProvider socialProvider = member.getSocialProvider();

        transactionTemplate.executeWithoutResult(status -> {
            succeedGroupHosts(memberId);
            purgeMeetingParticipation(memberId);
            purgePersonalData(memberId);
            anonymizeMember(member);
        });

        if (profileObjectKey != null) {
            storageService.delete(profileObjectKey);
        }

        if (socialProvider == SocialProvider.APPLE && appleAuthorizationCode != null) {
            appleTokenRevoker.revoke(appleAuthorizationCode);
        } else {
            log.info("Apple revoke skip — provider={}, codePresent={}", socialProvider, appleAuthorizationCode != null);
        }

        log.info("회원 탈퇴 완료: memberId={}", memberId);
    }

    /** R6 — 탈퇴자가 HOST인 그룹마다 잔여 구성원에게 승계하거나, 없으면 그룹을 종료한다. */
    private void succeedGroupHosts(Long memberId) {
        List<GroupMember> hostMemberships = groupMemberRepository.findByMemberId(memberId).stream()
                .filter(gm -> gm.getRole() == GroupMemberRole.HOST)
                .toList();

        for (GroupMember hostMembership : hostMemberships) {
            List<GroupMember> candidates = groupMemberRepository.findByGroupId(hostMembership.getGroupId()).stream()
                    .filter(gm -> !gm.getMemberId().equals(memberId))
                    .sorted(Comparator.comparing(GroupMember::getJoinedAt))
                    .toList();

            if (candidates.isEmpty()) {
                groupRepository.findById(hostMembership.getGroupId()).ifPresent(group -> {
                    group.close();
                    groupRepository.save(group);
                });
            } else {
                GroupMember successor = candidates.get(0);
                successor.promoteToHost();
                groupMemberRepository.save(successor);
            }
        }
    }

    /** R4 — 모임 참여 이력은 유지하되 출발지(좌표·메타)와 이동부담 스냅샷을 파기한다. */
    private void purgeMeetingParticipation(Long memberId) {
        List<MeetingParticipant> participants = meetingParticipantRepository.findByMemberId(memberId);
        participants.forEach(MeetingParticipant::clearDeparture);
        meetingParticipantRepository.saveAll(participants);

        meetingTravelBurdenRepository.deleteAllByMemberId(memberId);
    }

    /** R3 — 회원 개인 소유 데이터(출발지·약관 동의 이력·리프레시 토큰)를 물리 삭제한다. */
    private void purgePersonalData(Long memberId) {
        departurePlaceRepository.deleteAllByMemberId(memberId);
        termsAgreementRepository.deleteAllByMemberId(memberId);
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }

    /** R2 — 회원 레코드를 익명화한다 (뼈대만 유지). */
    private void anonymizeMember(Member member) {
        member.withdraw("withdrawn_" + UUID.randomUUID());
        memberRepository.save(member);
    }
}
