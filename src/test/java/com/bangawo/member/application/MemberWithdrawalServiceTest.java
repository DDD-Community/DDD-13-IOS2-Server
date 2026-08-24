package com.bangawo.member.application;

import com.bangawo.auth.application.AppleTokenRevoker;
import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.auth.domain.MemberStatus;
import com.bangawo.auth.domain.RefreshTokenRepository;
import com.bangawo.auth.domain.SocialProvider;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.GroupStatus;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingTravelBurdenRepository;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import com.bangawo.member.domain.terms.TermsAgreementRepository;
import com.bangawo.storage.application.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock DeparturePlaceRepository departurePlaceRepository;
    @Mock TermsAgreementRepository termsAgreementRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock GroupRepository groupRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock MeetingTravelBurdenRepository meetingTravelBurdenRepository;
    @Mock StorageService storageService;
    @Mock AppleTokenRevoker appleTokenRevoker;
    @Mock TransactionTemplate transactionTemplate;

    private MemberWithdrawalService newService() {
        return new MemberWithdrawalService(
                memberRepository, refreshTokenRepository, departurePlaceRepository,
                termsAgreementRepository, groupMemberRepository, groupRepository,
                meetingParticipantRepository, meetingTravelBurdenRepository,
                storageService, appleTokenRevoker, transactionTemplate);
    }

    /** TransactionTemplate.executeWithoutResult 가 실제 콜백을 실행하도록 스텁 */
    @SuppressWarnings("unchecked")
    private void stubTransactionTemplateToRunCallback() {
        doAnswer(invocation -> {
            Consumer<Object> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    private Member activeMember(Long id, SocialProvider provider, String profileImageUrl) {
        return Member.builder()
                .id(id).socialProvider(provider).socialUserId("social-" + id)
                .nickname("닉네임").email("a@a.com").profileImageUrl(profileImageUrl)
                .status(MemberStatus.ACTIVE).isRegistered(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void 존재하지_않는_회원이면_MEMBER_NOT_FOUND() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().withdraw(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void 이미_탈퇴한_회원이면_MEMBER_ALREADY_WITHDRAWN() {
        Member withdrawn = activeMember(1L, SocialProvider.KAKAO, null);
        withdrawn.withdraw("withdrawn_xxx");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(withdrawn));

        assertThatThrownBy(() -> newService().withdraw(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    @Test
    void 정상_탈퇴시_회원이_익명화되고_개인데이터가_파기된다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, "profiles/1/a.png");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();
        assertThat(saved.isWithdrawn()).isTrue();
        assertThat(saved.getNickname()).isNull();
        assertThat(saved.getEmail()).isNull();
        assertThat(saved.getProfileImageUrl()).isNull();

        verify(departurePlaceRepository).deleteAllByMemberId(1L);
        verify(termsAgreementRepository).deleteAllByMemberId(1L);
        verify(refreshTokenRepository).deleteAllByMemberId(1L);
        verify(meetingTravelBurdenRepository).deleteAllByMemberId(1L);
        verify(storageService).delete("profiles/1/a.png");
    }

    @Test
    void 호스트인_그룹에_잔여_구성원이_있으면_가장_먼저_참여한_구성원에게_승계한다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        GroupMember hostMembership = GroupMember.builder()
                .id(10L).groupId(100L).memberId(1L)
                .role(com.bangawo.group.domain.GroupMemberRole.HOST)
                .joinedAt(LocalDateTime.now().minusDays(5))
                .build();
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of(hostMembership));

        GroupMember earlyMember = GroupMember.builder()
                .id(11L).groupId(100L).memberId(2L)
                .role(com.bangawo.group.domain.GroupMemberRole.MEMBER)
                .joinedAt(LocalDateTime.now().minusDays(3))
                .build();
        GroupMember laterMember = GroupMember.builder()
                .id(12L).groupId(100L).memberId(3L)
                .role(com.bangawo.group.domain.GroupMemberRole.MEMBER)
                .joinedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(groupMemberRepository.findByGroupId(100L)).thenReturn(List.of(hostMembership, earlyMember, laterMember));
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(2L);
        assertThat(captor.getValue().getRole()).isEqualTo(com.bangawo.group.domain.GroupMemberRole.HOST);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void 호스트인_그룹에_잔여_구성원이_없으면_그룹을_CLOSED_처리한다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        GroupMember hostMembership = GroupMember.builder()
                .id(10L).groupId(100L).memberId(1L)
                .role(com.bangawo.group.domain.GroupMemberRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of(hostMembership));
        when(groupMemberRepository.findByGroupId(100L)).thenReturn(List.of(hostMembership));

        Group group = Group.builder()
                .id(100L).name("모임").themeTagCode("DINING").status(GroupStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(GroupStatus.CLOSED);
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void 모임_참여_출발지가_파기되고_참여이력은_유지된다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());

        MeetingParticipant participant = MeetingParticipant.create(
                500L, 1L, 37.5, 127.0, "PRESENT", "집", "카카오장소", "주소");
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of(participant));
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        verify(meetingParticipantRepository).saveAll(List.of(participant));
        assertThat(participant.hasCoordinate()).isFalse();
        assertThat(participant.getMeetingId()).isEqualTo(500L);
    }

    @Test
    void Apple회원이고_authorizationCode가_있으면_revoke를_호출한다() {
        Member member = activeMember(1L, SocialProvider.APPLE, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, "auth-code-123");

        verify(appleTokenRevoker, times(1)).revoke("auth-code-123");
    }

    @Test
    void Apple회원이_아니면_authorizationCode가_있어도_revoke를_호출하지_않는다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, "auth-code-123");

        verify(appleTokenRevoker, never()).revoke(any());
    }

    @Test
    void authorizationCode가_없으면_Apple회원이어도_revoke를_호출하지_않는다() {
        Member member = activeMember(1L, SocialProvider.APPLE, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        verify(appleTokenRevoker, never()).revoke(any());
    }

    @Test
    void 프로필이미지가_없으면_storageService_delete를_호출하지_않는다() {
        Member member = activeMember(1L, SocialProvider.KAKAO, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(groupMemberRepository.findByMemberId(1L)).thenReturn(List.of());
        when(meetingParticipantRepository.findByMemberId(1L)).thenReturn(List.of());
        stubTransactionTemplateToRunCallback();

        newService().withdraw(1L, null);

        verify(storageService, never()).delete(any());
    }
}
