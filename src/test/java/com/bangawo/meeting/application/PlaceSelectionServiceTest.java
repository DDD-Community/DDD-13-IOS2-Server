package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.meeting.domain.*;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.domain.RecommendationCandidate;
import com.bangawo.subway.domain.StationCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceSelectionServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock DeparturePlaceRepository departurePlaceRepository;
    @Mock MeetingParticipantRepository meetingParticipantRepository;
    @Mock MidpointCalculationService midpointCalculationService;
    @Mock MidpointStationCandidateRepository midpointStationCandidateRepository;
    @Mock PlaceRepository placeRepository;
    @Mock MeetingPlaceRecommendationRepository meetingPlaceRecommendationRepository;
    @Mock ThemeTagRepository themeTagRepository;

    PlaceSelectionService service;

    @BeforeEach
    void setUp() {
        service = new PlaceSelectionService(
                meetingRepository, groupMemberRepository, departurePlaceRepository,
                meetingParticipantRepository, midpointCalculationService, midpointStationCandidateRepository,
                placeRepository, meetingPlaceRecommendationRepository, themeTagRepository);
    }

    private Meeting meetingReadyForLocationPhase() {
        return Meeting.builder()
                .id(1L).groupId(10L).name("팀 회식").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.BEFORE).dateVoteStatus(DateVoteStatus.COMPLETED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private GroupMember host() {
        return GroupMember.builder()
                .id(1L).groupId(10L).memberId(20L).role(GroupMemberRole.HOST)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private List<StationCandidate> threeStations() {
        return List.of(
                new StationCandidate(100L, "강남역", "2호선", 0.5, 37.4979, 127.0276),
                new StationCandidate(101L, "역삼역", "2호선", 1.2, 37.5006, 127.0365),
                new StationCandidate(102L, "선릉역", "2호선", 1.8, 37.5045, 127.0490)
        );
    }

    private Place place(Long id) {
        return Place.builder().id(id).placeId(id).name("place-" + id).categoryLabel("한식").build();
    }

    @Test
    void 호스트가_아니면_NOT_GROUP_HOST() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meetingReadyForLocationPhase()));
        GroupMember member = GroupMember.builder()
                .id(2L).groupId(10L).memberId(21L).role(GroupMemberRole.MEMBER)
                .joinedAt(LocalDateTime.now()).build();
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 21L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.startLocationPhase(1L, 21L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_GROUP_HOST);
    }

    @Test
    void radiusKm이_6_초과면_INVALID_INPUT() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meetingReadyForLocationPhase()));
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host()));

        assertThatThrownBy(() -> service.startLocationPhase(1L, 20L, 7.0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void 출발지_미등록_참여자가_있으면_PARTICIPANT_DEPARTURE_NOT_SET() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meetingReadyForLocationPhase()));
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host()));
        MeetingParticipant noDeparture = MeetingParticipant.create(1L, 20L, null, null, AttendanceStatus.JOIN.name(), null, null, null);
        when(meetingParticipantRepository.findByMeetingId(1L)).thenReturn(List.of(noDeparture));

        assertThatThrownBy(() -> service.startLocationPhase(1L, 20L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PARTICIPANT_DEPARTURE_NOT_SET);
    }

    @Test
    void 첫번째_반경에서_후보가_있으면_바로_저장하고_RECOMMENDED로_전환() {
        Meeting meeting = meetingReadyForLocationPhase();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host()));
        MeetingParticipant participant = MeetingParticipant.create(1L, 20L, 37.5, 127.0, AttendanceStatus.JOIN.name(), null, null, null);
        when(meetingParticipantRepository.findByMeetingId(1L)).thenReturn(List.of(participant));
        when(midpointCalculationService.calculate(1L)).thenReturn(threeStations());

        when(placeRepository.findCandidates(anyList(), eq(2000.0), eq(null), eq(null)))
                .thenReturn(List.of(new RecommendationCandidate(place(500L), 100L)));

        service.startLocationPhase(1L, 20L, null);

        verify(placeRepository, times(1)).findCandidates(anyList(), anyDouble(), eq(null), eq(null));
        verify(meetingPlaceRecommendationRepository).saveAll(anyList());
        verify(meetingRepository).save(meeting);
        assertThat(meeting.getLocationStatus()).isEqualTo(LocationStatus.RECOMMENDED);
    }

    @Test
    void 첫번째_반경이_비면_다음_반경으로_확대() {
        Meeting meeting = meetingReadyForLocationPhase();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host()));
        MeetingParticipant participant = MeetingParticipant.create(1L, 20L, 37.5, 127.0, AttendanceStatus.JOIN.name(), null, null, null);
        when(meetingParticipantRepository.findByMeetingId(1L)).thenReturn(List.of(participant));
        when(midpointCalculationService.calculate(1L)).thenReturn(threeStations());

        when(placeRepository.findCandidates(anyList(), eq(2000.0), eq(null), eq(null))).thenReturn(List.of());
        when(placeRepository.findCandidates(anyList(), eq(4000.0), eq(null), eq(null)))
                .thenReturn(List.of(new RecommendationCandidate(place(500L), 100L)));

        service.startLocationPhase(1L, 20L, null);

        verify(placeRepository, times(2)).findCandidates(anyList(), anyDouble(), eq(null), eq(null));
        assertThat(meeting.getLocationStatus()).isEqualTo(LocationStatus.RECOMMENDED);
    }

    @Test
    void 반경6km까지_0건이면_PLACE_RECOMMENDATION_EMPTY() {
        Meeting meeting = meetingReadyForLocationPhase();
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(host()));
        MeetingParticipant participant = MeetingParticipant.create(1L, 20L, 37.5, 127.0, AttendanceStatus.JOIN.name(), null, null, null);
        when(meetingParticipantRepository.findByMeetingId(1L)).thenReturn(List.of(participant));
        when(midpointCalculationService.calculate(1L)).thenReturn(threeStations());
        when(placeRepository.findCandidates(anyList(), anyDouble(), eq(null), eq(null))).thenReturn(List.of());

        assertThatThrownBy(() -> service.startLocationPhase(1L, 20L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PLACE_RECOMMENDATION_EMPTY);

        verify(placeRepository, times(3)).findCandidates(anyList(), anyDouble(), eq(null), eq(null));
        verify(meetingPlaceRecommendationRepository, never()).saveAll(anyList());
    }
}
