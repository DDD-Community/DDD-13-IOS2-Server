package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.PlaceTravelBurdenResponse;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.subway.domain.SubwayGraph;
import com.bangawo.subway.domain.SubwayStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock MemberRepository memberRepository;

    @InjectMocks PlaceVoteService service;

    @Captor ArgumentCaptor<List<MeetingPlacePick>> picksCaptor;

    private Meeting recommendedMeeting;
    private Meeting votingMeeting;
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
        votingMeeting = Meeting.builder()
                .id(1L).groupId(10L).name("test").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.VOTING)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .confirmedDate(LocalDate.now().plusDays(10).atStartOfDay())
                .build();
        hostGroupMember = GroupMember.builder()
                .groupId(10L).memberId(1L).role(GroupMemberRole.HOST).build();
    }

    private MeetingPlacePick userPick(Long placeId) {
        return MeetingPlacePick.builder()
                .meetingId(1L).memberId(1L).placeId(placeId)
                .pickedAt(LocalDateTime.now()).source(PickSource.USER).build();
    }

    private MeetingPlaceRecommendation rec(Long placeId, int rank) {
        return MeetingPlaceRecommendation.builder()
                .meetingId(1L).placeId(placeId).rank(rank).build();
    }

    private Place place(Long id, String name) {
        return Place.builder().id(id).placeId(id).name(name)
                .categoryLabel("RESTAURANT").address("서울").latitude(37.5).longitude(127.0).build();
    }

    private Member member(Long id, String nickname) {
        return Member.builder().id(id).nickname(nickname).build();
    }

    // ---------- startVoting ----------

    @Test
    void startVoting_invalid_duration_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.startVoting(1L, 1L, 2))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startVoting_already_started_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(voteSessionRepository.findByMeetingId(1L))
                .willReturn(Optional.of(MeetingPlaceVoteSession.create(1L, 3)));

        assertThatThrownBy(() -> service.startVoting(1L, 1L, 3))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startVoting_success_even_with_zero_picks() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(recommendedMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(hostGroupMember));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of());
        given(recommendationRepository.findByMeetingIdOrderByRank(1L)).willReturn(List.of());
        given(subwayGraph.isLoaded()).willReturn(false);
        given(voteSessionRepository.save(any(MeetingPlaceVoteSession.class)))
                .willReturn(MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).build());

        service.startVoting(1L, 1L, 3);

        assertThat(recommendedMeeting.getLocationStatus()).isEqualTo(LocationStatus.VOTING);
        verify(voteSessionRepository).save(any(MeetingPlaceVoteSession.class));
    }

    // ---------- backfill (createSession) ----------

    @Test
    void createSession_backfills_to_min3_when_under() {
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of(userPick(100L)));
        given(recommendationRepository.findByMeetingIdOrderByRank(1L))
                .willReturn(List.of(rec(100L, 1), rec(200L, 2), rec(300L, 3), rec(400L, 4)));
        given(subwayGraph.isLoaded()).willReturn(false);
        given(voteSessionRepository.save(any())).willReturn(
                MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).build());

        service.createSession(1L, 3);

        verify(meetingPlacePickRepository).saveAll(picksCaptor.capture());
        List<MeetingPlacePick> saved = picksCaptor.getValue();
        // 이미 담긴 100 제외, 200·300 백필 → 2건 (총 3개 달성)
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(p -> p.getSource() == PickSource.SYSTEM && p.getMemberId() == null);
        assertThat(saved).extracting(MeetingPlacePick::getPlaceId).containsExactly(200L, 300L);
    }

    @Test
    void createSession_no_backfill_when_3_or_more() {
        given(meetingPlacePickRepository.findByMeetingId(1L))
                .willReturn(List.of(userPick(100L), userPick(200L), userPick(300L)));
        given(subwayGraph.isLoaded()).willReturn(false);
        given(voteSessionRepository.save(any())).willReturn(
                MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).build());

        service.createSession(1L, 3);

        verify(meetingPlacePickRepository, never()).saveAll(any());
    }

    @Test
    void createSession_backfill_limited_by_recommendation_count() {
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of());
        given(recommendationRepository.findByMeetingIdOrderByRank(1L))
                .willReturn(List.of(rec(200L, 1), rec(300L, 2)));   // 추천 2개뿐
        given(subwayGraph.isLoaded()).willReturn(false);
        given(voteSessionRepository.save(any())).willReturn(
                MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).build());

        service.createSession(1L, 3);

        verify(meetingPlacePickRepository).saveAll(picksCaptor.capture());
        assertThat(picksCaptor.getValue()).hasSize(2);   // 3개 못 채워도 가능한 만큼만
    }

    // ---------- submitVote ----------

    private void givenVotingSession() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(votingMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L))
                .willReturn(Optional.of(hostGroupMember));
        given(voteSessionRepository.findByMeetingId(1L))
                .willReturn(Optional.of(MeetingPlaceVoteSession.builder()
                        .id(99L).meetingId(1L).status("IN_PROGRESS")
                        .deadline(LocalDateTime.now().plusDays(1)).build()));
    }

    @Test
    void submitVote_invalid_candidate_throws() {
        givenVotingSession();
        given(meetingPlacePickRepository.findByMeetingId(1L))
                .willReturn(List.of(userPick(100L), userPick(200L), userPick(300L), userPick(400L)));

        assertThatThrownBy(() -> service.submitVote(1L, 1L, List.of(999L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_VOTE_INVALID_CANDIDATE);
    }

    @Test
    void submitVote_limit_exceeded_throws() {
        givenVotingSession();
        // 후보 4개 → maxVotes = 2, 3개 제출 시 초과
        given(meetingPlacePickRepository.findByMeetingId(1L))
                .willReturn(List.of(userPick(100L), userPick(200L), userPick(300L), userPick(400L)));

        assertThatThrownBy(() -> service.submitVote(1L, 1L, List.of(100L, 200L, 300L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_VOTE_LIMIT_EXCEEDED);
    }

    @Test
    void submitVote_success() {
        givenVotingSession();
        given(meetingPlacePickRepository.findByMeetingId(1L))
                .willReturn(List.of(userPick(100L), userPick(200L), userPick(300L), userPick(400L)));
        given(meetingParticipantRepository.findByMeetingId(1L)).willReturn(List.of(
                MeetingParticipant.create(1L, 1L, 37.5, 127.0, "ATTENDING"),
                MeetingParticipant.create(1L, 2L, 37.6, 127.1, "ATTENDING")));
        given(voteRepository.countDistinctVotersBySessionId(99L)).willReturn(1L);

        List<MeetingPlaceVote> votes = service.submitVote(1L, 1L, List.of(100L, 200L));

        assertThat(votes).hasSize(2);
        verify(voteRepository).deleteBySessionIdAndMemberId(99L, 1L);
        verify(voteRepository).saveAll(any());
        verify(placeConfirmService, never()).confirmPlace(anyLong());
    }

    // ---------- getVoteStatus (memberStatuses 전원 공개) ----------

    @Test
    void getVoteStatus_provides_memberStatuses_to_all_callers() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(votingMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L))
                .willReturn(Optional.of(hostGroupMember));
        given(voteSessionRepository.findByMeetingId(1L))
                .willReturn(Optional.of(MeetingPlaceVoteSession.builder()
                        .id(99L).meetingId(1L).status("IN_PROGRESS")
                        .deadline(LocalDateTime.now().plusDays(1)).build()));
        given(meetingPlacePickRepository.findByMeetingId(1L))
                .willReturn(List.of(userPick(100L), userPick(200L)));
        given(voteRepository.findBySessionId(99L))
                .willReturn(List.of(MeetingPlaceVote.of(99L, 1L, 100L)));
        given(travelBurdenRepository.findByMeetingId(1L)).willReturn(List.of());
        given(placeRepository.findByIds(any()))
                .willReturn(List.of(place(100L, "가게A"), place(200L, "나게B")));
        given(meetingParticipantRepository.findByMeetingId(1L)).willReturn(List.of(
                MeetingParticipant.create(1L, 1L, 37.5, 127.0, "ATTENDING"),
                MeetingParticipant.create(1L, 2L, 37.6, 127.1, "ATTENDING")));
        given(memberRepository.findAllById(any()))
                .willReturn(List.of(member(1L, "홍길동"), member(2L, "김철수")));

        PlaceVoteStatusResponse res = service.getVoteStatus(1L, 1L);

        assertThat(res.memberStatuses()).hasSize(2);
        assertThat(res.memberStatuses())
                .anySatisfy(m -> { assertThat(m.memberId()).isEqualTo(1L); assertThat(m.completed()).isTrue(); })
                .anySatisfy(m -> { assertThat(m.memberId()).isEqualTo(2L); assertThat(m.completed()).isFalse(); });
        assertThat(res.candidates()).hasSize(2);
    }

    // ---------- getPlaceTravelBurden (친구들 거리보기) ----------

    @Test
    void getPlaceTravelBurden_returns_member_burdens_with_longest_flag() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(votingMeeting));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L))
                .willReturn(Optional.of(hostGroupMember));
        given(travelBurdenRepository.findByMeetingIdAndPlaceId(1L, 100L)).willReturn(List.of(
                MeetingTravelBurden.of(1L, 1L, 100L, 1800, 1),
                MeetingTravelBurden.of(1L, 2L, 100L, 3000, 2)));
        given(memberRepository.findAllById(any()))
                .willReturn(List.of(member(1L, "홍길동"), member(2L, "김철수")));
        given(placeRepository.findByIds(List.of(100L))).willReturn(List.of(place(100L, "가게A")));

        PlaceTravelBurdenResponse res = service.getPlaceTravelBurden(1L, 100L, 1L);

        assertThat(res.place().placeId()).isEqualTo(100L);
        assertThat(res.burdens()).hasSize(2);
        assertThat(res.burdens())
                .anySatisfy(b -> { assertThat(b.memberId()).isEqualTo(2L); assertThat(b.isLongest()).isTrue(); })
                .anySatisfy(b -> { assertThat(b.memberId()).isEqualTo(1L); assertThat(b.isLongest()).isFalse(); });
    }
}
