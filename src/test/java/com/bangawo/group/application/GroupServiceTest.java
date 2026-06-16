package com.bangawo.group.application;

import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock GroupRepository groupRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock MeetingRepository meetingRepository;
    @Mock ThemeTagRepository themeTagRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock DeparturePlaceRepository departurePlaceRepository;
    @InjectMocks GroupService groupService;

    @Test
    void createGroupWithMeeting_categoryLabels_vibes_reservable_parking이_Meeting에_전달된다() {
        Group savedGroup = Group.builder()
                .id(1L).name("팀 회식").themeTagCode("DINING")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(groupRepository.save(any())).thenReturn(savedGroup);
        when(meetingRepository.save(any())).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            return Meeting.builder()
                    .id(100L).groupId(m.getGroupId()).name(m.getName()).themeTagCode(m.getThemeTagCode())
                    .categoryLabels(m.getCategoryLabels()).vibes(m.getVibes())
                    .reservable(m.getReservable()).parking(m.getParking())
                    .status(m.getStatus()).locationStatus(m.getLocationStatus()).dateVoteStatus(m.getDateVoteStatus())
                    .createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt())
                    .build();
        });
        when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(departurePlaceRepository.findDefaultByMemberId(20L)).thenReturn(Optional.empty());
        when(meetingParticipantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        groupService.createGroupWithMeeting(20L, "팀 회식", "DINING",
                List.of("한식", "주점"), List.of("왁자지껄"), true, null);

        ArgumentCaptor<Meeting> captor = ArgumentCaptor.forClass(Meeting.class);
        verify(meetingRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryLabels()).containsExactly("한식", "주점");
        assertThat(captor.getValue().getVibes()).containsExactly("왁자지껄");
        assertThat(captor.getValue().getReservable()).isTrue();
        assertThat(captor.getValue().getParking()).isNull();
    }

    @Test
    void createNextMeeting_categoryLabels_vibes_reservable_parking이_Meeting에_전달된다() {
        GroupMember host = GroupMember.builder()
                .id(1L).groupId(10L).memberId(20L).role(GroupMemberRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host));

        Meeting closedMeeting = Meeting.builder()
                .id(5L).groupId(10L).name("이전 모임").themeTagCode("DINING")
                .status(com.bangawo.meeting.domain.MeetingStatus.CLOSED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(meetingRepository.findLatestByGroupId(10L)).thenReturn(Optional.of(closedMeeting));
        when(meetingRepository.save(any())).thenAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            return Meeting.builder()
                    .id(200L).groupId(m.getGroupId()).name(m.getName()).themeTagCode(m.getThemeTagCode())
                    .categoryLabels(m.getCategoryLabels()).vibes(m.getVibes())
                    .reservable(m.getReservable()).parking(m.getParking())
                    .status(m.getStatus()).locationStatus(m.getLocationStatus()).dateVoteStatus(m.getDateVoteStatus())
                    .createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt())
                    .build();
        });

        groupService.createNextMeeting(10L, 20L, "다음 모임", "DINING",
                List.of("카페"), List.of("조용한"), null, false);

        ArgumentCaptor<Meeting> captor = ArgumentCaptor.forClass(Meeting.class);
        verify(meetingRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryLabels()).containsExactly("카페");
        assertThat(captor.getValue().getVibes()).containsExactly("조용한");
        assertThat(captor.getValue().getReservable()).isNull();
        assertThat(captor.getValue().getParking()).isFalse();
    }
}
