package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.subway.domain.SubwayGraph;
import com.bangawo.subway.domain.SubwayStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceVoteServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock MeetingPlacePickRepository meetingPlacePickRepository;
    @Mock MeetingPlaceVoteSessionRepository voteSessionRepository;
    @Mock MeetingPlaceVoteRepository voteRepository;
    @Mock MeetingTravelBurdenRepository travelBurdenRepository;
    @Mock MeetingPlaceRecommendationRepository recommendationRepository;
    @Mock SubwayGraph subwayGraph;
    @Mock SubwayStationRepository subwayStationRepository;
    @Mock PlaceConfirmService placeConfirmService;
    @Mock PlaceRepository placeRepository;

    @InjectMocks PlaceVoteService service;

    private Meeting recommendedMeeting;
    private GroupMember hostGroupMember;

    @BeforeEach
    void setUp() {
        recommendedMeeting = Meeting.builder()
                .id(1L).groupId(10L).name("test").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .confirmedDate(LocalDate.now().plusDays(10).atStartOfDay())
                .pickDeadline(LocalDateTime.now().plusDays(2))
                .build();
        hostGroupMember = GroupMember.builder()
                .groupId(10L).memberId(1L).role(GroupMemberRole.HOST).build();
    }

    @Test
    void startVoting_invalid_duration_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(meetingPlacePickRepository.existsByMeetingId(1L)).willReturn(true);
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.startVoting(1L, 1L, 2))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startVoting_already_started_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(meetingPlacePickRepository.existsByMeetingId(1L)).willReturn(true);
        given(voteSessionRepository.findByMeetingId(1L))
                .willReturn(Optional.of(MeetingPlaceVoteSession.create(1L, 3)));

        assertThatThrownBy(() -> service.startVoting(1L, 1L, 3))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startVoting_success() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(meetingPlacePickRepository.existsByMeetingId(1L)).willReturn(true);
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());
        given(voteSessionRepository.save(any(MeetingPlaceVoteSession.class)))
                .willReturn(MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).build());

        service.startVoting(1L, 1L, 3);

        assertThat(recommendedMeeting.getLocationStatus()).isEqualTo(LocationStatus.VOTING);
        verify(meetingRepository).save(recommendedMeeting);
        verify(voteSessionRepository).save(any(MeetingPlaceVoteSession.class));
    }
}
