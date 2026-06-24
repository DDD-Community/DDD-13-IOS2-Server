package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.PlaceResultResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaceConfirmServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock MeetingPlacePickRepository meetingPlacePickRepository;
    @Mock MeetingPlaceVoteRepository voteRepository;
    @Mock MeetingPlaceVoteSessionRepository voteSessionRepository;
    @Mock MeetingTravelBurdenRepository travelBurdenRepository;
    @Mock MeetingConfirmedPlaceRepository confirmedPlaceRepository;
    @Mock PlaceRepository placeRepository;

    @InjectMocks PlaceConfirmService service;

    @Captor ArgumentCaptor<MeetingConfirmedPlace> confirmedCaptor;

    private GroupMember host;

    @BeforeEach
    void setUp() {
        host = GroupMember.builder().groupId(10L).memberId(1L).role(GroupMemberRole.HOST).build();
        given(placeRepository.findByIds(anyList())).willAnswer(inv -> {
            List<Long> ids = inv.getArgument(0);
            return ids.stream().map(id -> place(id)).toList();
        });
        given(confirmedPlaceRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    }

    private Place place(Long id) {
        return Place.builder().id(id).placeId(id).name("place" + id)
                .categoryLabel("RESTAURANT").address("서울").latitude(37.5).longitude(127.0).build();
    }

    private MeetingPlacePick pick(Long placeId, LocalDateTime pickedAt) {
        return MeetingPlacePick.builder()
                .meetingId(1L).memberId(1L).placeId(placeId).pickedAt(pickedAt)
                .source(PickSource.USER).build();
    }

    private Meeting meeting(LocationStatus status) {
        return Meeting.builder()
                .id(1L).groupId(10L).name("t").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE).locationStatus(status)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .confirmedDate(LocalDate.now().plusDays(10).atStartOfDay())
                .build();
    }

    // ---------- 동점 4단계: 최초 담은 시각 ----------

    @Test
    void confirmPlace_tie_broken_by_earliest_pickedAt() {
        LocalDateTime base = LocalDateTime.now();
        // 둘 다 0표·이동부담 없음 → 4순위(최초 담은 시각)로 결정. 100이 더 일찍 담김.
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of(
                pick(200L, base),
                pick(100L, base.minusMinutes(10))));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());
        given(travelBurdenRepository.findByMeetingId(1L)).willReturn(List.of());

        service.confirmPlace(1L);

        verify(confirmedPlaceRepository).save(confirmedCaptor.capture());
        assertThat(confirmedCaptor.getValue().getPlaceId()).isEqualTo(100L);
    }

    @Test
    void confirmPlace_winner_by_vote_count() {
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of(
                pick(100L, LocalDateTime.now().minusMinutes(10)),
                pick(200L, LocalDateTime.now())));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.of(
                MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).status("IN_PROGRESS").build()));
        given(voteRepository.findBySessionId(99L)).willReturn(List.of(
                MeetingPlaceVote.of(99L, 1L, 200L),
                MeetingPlaceVote.of(99L, 2L, 200L),
                MeetingPlaceVote.of(99L, 3L, 100L)));
        given(travelBurdenRepository.findByMeetingId(1L)).willReturn(List.of());

        service.confirmPlace(1L);

        verify(confirmedPlaceRepository).save(confirmedCaptor.capture());
        assertThat(confirmedCaptor.getValue().getPlaceId()).isEqualTo(200L);
    }

    // ---------- 1~3위 rank 산출 ----------

    @Test
    void getResult_assigns_rank_1_to_3() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting(LocationStatus.CONFIRMED)));
        given(confirmedPlaceRepository.findByMeetingId(1L)).willReturn(Optional.of(
                MeetingConfirmedPlace.of(1L, 100L, "place100", "서울")));
        LocalDateTime base = LocalDateTime.now();
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of(
                pick(100L, base.minusMinutes(40)),
                pick(200L, base.minusMinutes(30)),
                pick(300L, base.minusMinutes(20)),
                pick(400L, base.minusMinutes(10))));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.of(
                MeetingPlaceVoteSession.builder().id(99L).meetingId(1L).status("IN_PROGRESS").build()));
        // 득표: 100=3, 200=2, 300=1, 400=0 → rank 1,2,3,0
        given(voteRepository.findBySessionId(99L)).willReturn(List.of(
                MeetingPlaceVote.of(99L, 1L, 100L), MeetingPlaceVote.of(99L, 2L, 100L), MeetingPlaceVote.of(99L, 3L, 100L),
                MeetingPlaceVote.of(99L, 4L, 200L), MeetingPlaceVote.of(99L, 5L, 200L),
                MeetingPlaceVote.of(99L, 6L, 300L)));
        given(travelBurdenRepository.findByMeetingId(1L)).willReturn(List.of());

        PlaceResultResponse res = service.getResult(1L, 1L);

        assertThat(res.candidates()).hasSize(4);
        assertThat(res.candidates().get(0).rank()).isEqualTo(1);
        assertThat(res.candidates().get(0).place().placeId()).isEqualTo(100L);
        assertThat(res.candidates().get(1).rank()).isEqualTo(2);
        assertThat(res.candidates().get(2).rank()).isEqualTo(3);
        assertThat(res.candidates().get(3).rank()).isEqualTo(0);   // 4위 이하 = rank 0
    }

    // ---------- 수동 확정 ----------

    @Test
    void confirmByHost_not_host_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting(LocationStatus.VOTING)));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 2L)).willReturn(Optional.of(
                GroupMember.builder().groupId(10L).memberId(2L).role(GroupMemberRole.MEMBER).build()));

        assertThatThrownBy(() -> service.confirmByHost(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_HOST);
    }

    @Test
    void confirmByHost_not_voting_throws() {
        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting(LocationStatus.RECOMMENDED)));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(host));

        assertThatThrownBy(() -> service.confirmByHost(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
    }

    @Test
    void confirmByHost_success_confirms_and_transitions() {
        Meeting m = meeting(LocationStatus.VOTING);
        given(meetingRepository.findById(1L)).willReturn(Optional.of(m));
        given(groupMemberRepository.findByGroupIdAndMemberId(10L, 1L)).willReturn(Optional.of(host));
        given(meetingPlacePickRepository.findByMeetingId(1L)).willReturn(List.of(
                pick(100L, LocalDateTime.now())));
        given(voteSessionRepository.findByMeetingId(1L)).willReturn(Optional.empty());
        given(travelBurdenRepository.findByMeetingId(1L)).willReturn(List.of());

        service.confirmByHost(1L, 1L);

        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.CONFIRMED);
        verify(confirmedPlaceRepository).save(any());
    }
}
