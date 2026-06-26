package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.auth.domain.MemberStatus;
import com.bangawo.auth.domain.SocialProvider;
import com.bangawo.group.domain.*;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.MeetingCardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MeetingListServiceTest {

    @InjectMocks
    private MeetingListService meetingListService;

    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingParticipantRepository meetingParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ThemeTagRepository themeTagRepository;

    @Test
    @DisplayName("소속 그룹 없으면 빈 리스트 반환")
    void emptyWhenNoMemberships() {
        given(groupMemberRepository.findByMemberId(1L)).willReturn(List.of());

        List<MeetingCardResponse> result = meetingListService.getMyMeetingList(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("IN_PROGRESS → CONFIRMED → CLOSED 순으로 정렬")
    void sortedByListStatus() {
        long memberId = 1L;
        LocalDateTime now = LocalDateTime.now();

        GroupMember myMembership1 = membership(1L, 1L, memberId, now.minusDays(3));
        GroupMember myMembership2 = membership(2L, 2L, memberId, now.minusDays(2));
        GroupMember myMembership3 = membership(3L, 3L, memberId, now.minusDays(1));

        given(groupMemberRepository.findByMemberId(memberId))
                .willReturn(List.of(myMembership1, myMembership2, myMembership3));

        Group group1 = group(1L, "그룹A", "DINING");
        Group group2 = group(2L, "그룹B", "STUDY");
        Group group3 = group(3L, "그룹C", "EXERCISE");
        given(groupRepository.findAllById(any())).willReturn(List.of(group1, group2, group3));

        // group1 → CLOSED (confirmedDate 지남), group2 → CONFIRMED, group3 → IN_PROGRESS
        Meeting closedMeeting = meeting(10L, 1L, LocationStatus.CONFIRMED, DateVoteStatus.COMPLETED,
                LocalDate.now().minusDays(1), now.minusDays(5));
        Meeting confirmedMeeting = meeting(20L, 2L, LocationStatus.CONFIRMED, DateVoteStatus.COMPLETED,
                null, now.minusDays(4));
        Meeting inProgressMeeting = meeting(30L, 3L, LocationStatus.BEFORE, DateVoteStatus.BEFORE,
                null, now.minusDays(3));
        given(meetingRepository.findLatestByGroupIdIn(any()))
                .willReturn(List.of(closedMeeting, confirmedMeeting, inProgressMeeting));

        given(groupMemberRepository.findByGroupIdIn(any()))
                .willReturn(List.of(myMembership1, myMembership2, myMembership3));

        Member member = member(memberId, "홍길동", "https://example.com/1.jpg");
        given(memberRepository.findAllById(any())).willReturn(List.of(member));

        ThemeTag dining = themeTag("DINING", "회식");
        ThemeTag study = themeTag("STUDY", "스터디");
        ThemeTag exercise = themeTag("EXERCISE", "운동");
        given(themeTagRepository.findByCodeIn(any(Set.class)))
                .willReturn(List.of(dining, study, exercise));

        List<MeetingCardResponse> result = meetingListService.getMyMeetingList(memberId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).listStatus()).isEqualTo(MeetingListStatus.IN_PROGRESS);
        assertThat(result.get(1).listStatus()).isEqualTo(MeetingListStatus.CONFIRMED);
        assertThat(result.get(2).listStatus()).isEqualTo(MeetingListStatus.CLOSED);
    }

    @Test
    @DisplayName("탈퇴 회원은 nickname/profileImageUrl null 처리")
    void withdrawnMemberHasNullProfile() {
        long memberId = 1L;
        long withdrawnMemberId = 99L;
        LocalDateTime now = LocalDateTime.now();

        GroupMember myMembership = membership(1L, 1L, memberId, now.minusDays(1));
        GroupMember withdrawnMembership = membership(2L, 1L, withdrawnMemberId, now);

        given(groupMemberRepository.findByMemberId(memberId)).willReturn(List.of(myMembership));
        given(groupRepository.findAllById(any())).willReturn(List.of(group(1L, "그룹A", "DINING")));
        given(meetingRepository.findLatestByGroupIdIn(any()))
                .willReturn(List.of(meeting(10L, 1L, LocationStatus.BEFORE, DateVoteStatus.BEFORE, null, now)));
        given(groupMemberRepository.findByGroupIdIn(any()))
                .willReturn(List.of(myMembership, withdrawnMembership));
        given(memberRepository.findAllById(any())).willReturn(List.of(member(memberId, "홍길동", "https://img")));
        given(themeTagRepository.findByCodeIn(any(Set.class))).willReturn(List.of(themeTag("DINING", "회식")));

        List<MeetingCardResponse> result = meetingListService.getMyMeetingList(memberId);

        assertThat(result).hasSize(1);
        List<MeetingCardResponse.MemberInfo> members = result.get(0).members();
        assertThat(members).hasSize(2);

        MeetingCardResponse.MemberInfo withdrawn = members.stream()
                .filter(m -> m.memberId().equals(withdrawnMemberId))
                .findFirst().orElseThrow();
        assertThat(withdrawn.nickname()).isNull();
        assertThat(withdrawn.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("구성원은 joinedAt 오름차순으로 정렬")
    void membersSortedByJoinedAt() {
        long memberId = 1L;
        LocalDateTime now = LocalDateTime.now();

        GroupMember late = membership(2L, 1L, 2L, now);
        GroupMember early = membership(1L, 1L, memberId, now.minusDays(5));

        given(groupMemberRepository.findByMemberId(memberId)).willReturn(List.of(early));
        given(groupRepository.findAllById(any())).willReturn(List.of(group(1L, "그룹A", "DINING")));
        given(meetingRepository.findLatestByGroupIdIn(any()))
                .willReturn(List.of(meeting(10L, 1L, LocationStatus.BEFORE, DateVoteStatus.BEFORE, null, now)));
        given(groupMemberRepository.findByGroupIdIn(any())).willReturn(List.of(late, early));
        given(memberRepository.findAllById(any())).willReturn(List.of(
                member(memberId, "홍길동", "https://a"),
                member(2L, "김철수", "https://b")));
        given(themeTagRepository.findByCodeIn(any(Set.class))).willReturn(List.of(themeTag("DINING", "회식")));

        List<MeetingCardResponse> result = meetingListService.getMyMeetingList(memberId);

        List<MeetingCardResponse.MemberInfo> members = result.get(0).members();
        assertThat(members.get(0).memberId()).isEqualTo(memberId);
        assertThat(members.get(1).memberId()).isEqualTo(2L);
    }

    // --- helpers ---

    private GroupMember membership(Long id, Long groupId, Long memberId, LocalDateTime joinedAt) {
        return GroupMember.builder()
                .id(id).groupId(groupId).memberId(memberId)
                .role(GroupMemberRole.MEMBER)
                .joinedAt(joinedAt).build();
    }

    private Group group(Long id, String name, String themeTagCode) {
        return Group.builder().id(id).name(name).themeTagCode(themeTagCode)
                .status(GroupStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    private Meeting meeting(Long id, Long groupId, LocationStatus loc, DateVoteStatus date,
                            LocalDate confirmedDate, LocalDateTime createdAt) {
        return Meeting.builder()
                .id(id).groupId(groupId).name("테스트").themeTagCode("DINING")
                .locationStatus(loc).dateVoteStatus(date)
                .confirmedDate(confirmedDate != null ? confirmedDate.atStartOfDay() : null)
                .createdAt(createdAt).updatedAt(createdAt).build();
    }

    private Member member(Long id, String nickname, String profileImageUrl) {
        return Member.builder()
                .id(id).socialProvider(SocialProvider.KAKAO).socialUserId("s" + id)
                .nickname(nickname).profileImageUrl(profileImageUrl)
                .status(MemberStatus.ACTIVE).isRegistered(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    private ThemeTag themeTag(String code, String displayName) {
        return ThemeTag.builder().id(1L).code(code).displayName(displayName).sortOrder(1).active(true).build();
    }
}
