package com.bangawo.meeting.application;

import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.PlaceCardResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacePickServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock MeetingPlacePickRepository meetingPlacePickRepository;
    @Mock MeetingPlaceRecommendationRepository meetingPlaceRecommendationRepository;
    @Mock PlaceRepository placeRepository;
    @Mock MemberRepository memberRepository;
    @Mock PlaceVoteService placeVoteService;

    @InjectMocks PlacePickService service;

    private Meeting recommendedMeeting;
    private GroupMember memberGroupMember;
    private GroupMember hostGroupMember;

    @BeforeEach
    void setUp() {
        recommendedMeeting = Meeting.builder()
                .id(1L).groupId(10L).name("test").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .confirmedDate(LocalDate.now().plusDays(10))
                .pickDeadline(LocalDateTime.now().plusDays(2))
                .build();
        memberGroupMember = GroupMember.builder()
                .groupId(10L).memberId(2L).role(GroupMemberRole.MEMBER).build();
        hostGroupMember = GroupMember.builder()
                .groupId(10L).memberId(1L).role(GroupMemberRole.HOST).build();
    }

    @Test
    void getPlaces_stationId_filter() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));

        MeetingPlaceRecommendation rec1 = MeetingPlaceRecommendation.of(1L, 100L, 1, 0.9, 5L);
        MeetingPlaceRecommendation rec2 = MeetingPlaceRecommendation.of(1L, 200L, 2, 0.8, 6L);
        given(meetingPlaceRecommendationRepository.findByMeetingIdOrderByRank(1L))
                .willReturn(List.of(rec1, rec2));

        Place place1 = Place.builder().id(100L).name("A").categoryLabel("RESTAURANT")
                .address("Seoul").vibe(List.of("casual")).build();
        given(placeRepository.findByIds(List.of(100L))).willReturn(List.of(place1));
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of());

        List<PlaceCardResponse> result = service.getPlaces(1L, 2L, 5L, null, null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).placeId()).isEqualTo(100L);
    }

    @Test
    void pickPlace_success() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));
        given(meetingPlacePickRepository.existsByMeetingIdAndMemberIdAndPlaceId(1L, 2L, 100L)).willReturn(false);
        given(meetingParticipantRepository.findByMeetingId(1L)).willReturn(List.of());

        service.pickPlace(1L, 2L, 100L);
        verify(meetingPlacePickRepository).save(any(MeetingPlacePick.class));
    }

    @Test
    void pickPlace_idempotent() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));
        given(meetingPlacePickRepository.existsByMeetingIdAndMemberIdAndPlaceId(1L, 2L, 100L)).willReturn(true);

        service.pickPlace(1L, 2L, 100L);
        verify(meetingPlacePickRepository, never()).save(any());
    }

    @Test
    void pickPlace_not_recommended_throws() {
        Meeting beforeMeeting = Meeting.builder()
                .id(1L).groupId(10L).name("t").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE).locationStatus(LocationStatus.BEFORE)
                .dateVoteStatus(DateVoteStatus.BEFORE).build();
        given(meetingRepository.findById(1L)).willReturn(Optional.of(beforeMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));

        assertThatThrownBy(() -> service.pickPlace(1L, 2L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void pickPlace_deadline_expired_throws() {
        Meeting expired = Meeting.builder()
                .id(1L).groupId(10L).name("t").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE).locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .pickDeadline(LocalDateTime.now().minusSeconds(1)).build();
        given(meetingRepository.findById(1L)).willReturn(Optional.of(expired));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));

        assertThatThrownBy(() -> service.pickPlace(1L, 2L, 100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void pickPlace_all_done_auto_transitions_to_voting() {
        MeetingParticipant p1 = MeetingParticipant.create(1L, 2L, null, null, "JOIN");
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(memberGroupMember));
        given(meetingPlacePickRepository.existsByMeetingIdAndMemberIdAndPlaceId(1L, 2L, 100L)).willReturn(false);
        given(meetingParticipantRepository.findByMeetingId(1L)).willReturn(List.of(p1));
        given(meetingPlacePickRepository.countByMeetingIdAndMemberId(1L, 2L)).willReturn(1);
        given(placeVoteService.createSessionWithDefaultDuration(anyLong())).willReturn(null);

        service.pickPlace(1L, 2L, 100L);

        verify(meetingRepository).save(any(Meeting.class));
        assertThat(recommendedMeeting.getLocationStatus()).isEqualTo(LocationStatus.VOTING);
        verify(placeVoteService).createSessionWithDefaultDuration(1L);
    }

    @Test
    void startVoting_invalid_duration_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(meetingPlacePickRepository.existsByMeetingId(1L)).willReturn(true);

        assertThatThrownBy(() -> service.startVoting(1L, 1L, 2))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startVoting_success() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(meetingPlacePickRepository.existsByMeetingId(1L)).willReturn(true);
        given(placeVoteService.createSession(anyLong(), anyInt())).willReturn(null);

        service.startVoting(1L, 1L, 3);

        assertThat(recommendedMeeting.getLocationStatus()).isEqualTo(LocationStatus.VOTING);
        verify(meetingRepository).save(recommendedMeeting);
        verify(placeVoteService).createSession(1L, 3);
    }
}
