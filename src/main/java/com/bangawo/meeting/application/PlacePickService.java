package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.MemberPickStatus;
import com.bangawo.meeting.presentation.dto.PickStatusResponse;
import com.bangawo.meeting.presentation.dto.PlaceCardResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacePickService {

    private static final Set<Integer> VALID_DURATION_DAYS = Set.of(1, 3, 7);

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingPlacePickRepository meetingPlacePickRepository;
    private final MeetingPlaceRecommendationRepository meetingPlaceRecommendationRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final PlaceVoteService placeVoteService;

    @Transactional(readOnly = true)
    public List<PlaceCardResponse> getPlaces(Long meetingId, Long memberId,
                                              Long stationId, String category,
                                              Boolean reservable, Boolean parking) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<MeetingPlaceRecommendation> recommendations =
                meetingPlaceRecommendationRepository.findByMeetingIdOrderByRank(meetingId);

        if (stationId != null) {
            recommendations = recommendations.stream()
                    .filter(r -> stationId.equals(r.getNearestStationId()))
                    .toList();
        }

        List<Long> placeIds = recommendations.stream()
                .map(MeetingPlaceRecommendation::getPlaceId).toList();

        Map<Long, Place> placeById = placeRepository.findByIds(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, p -> p));

        List<MeetingPlacePick> allPicks = meetingPlacePickRepository.findByMeetingId(meetingId);
        Map<Long, Long> pickCountByPlaceId = allPicks.stream()
                .collect(Collectors.groupingBy(MeetingPlacePick::getPlaceId, Collectors.counting()));
        Set<Long> myPickedPlaceIds = allPicks.stream()
                .filter(p -> memberId.equals(p.getMemberId()))
                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toSet());

        return recommendations.stream()
                .map(r -> {
                    Place place = placeById.get(r.getPlaceId());
                    if (place == null) return null;
                    if (category != null && !category.equals(place.getCategoryLabel())) return null;
                    if (Boolean.TRUE.equals(reservable) && !Boolean.TRUE.equals(place.getReservable())) return null;
                    if (Boolean.TRUE.equals(parking) && !Boolean.TRUE.equals(place.getHasParking())) return null;
                    List<String> vibes = place.getVibe() != null
                            ? place.getVibe().stream().limit(3).toList() : List.of();
                    return new PlaceCardResponse(r.getPlaceId(), place.getName(),
                            place.getCategoryLabel(), place.getAddress(), vibes, null,
                            pickCountByPlaceId.getOrDefault(r.getPlaceId(), 0L).intValue(),
                            myPickedPlaceIds.contains(r.getPlaceId()));
                })
                .filter(r -> r != null).toList();
    }

    @Transactional
    public void pickPlace(Long meetingId, Long memberId, Long placeId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        if (meeting.getLocationStatus() != LocationStatus.RECOMMENDED) {
            throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
        }
        if (meeting.isPickDeadlineExpired()) {
            throw new BusinessException(ErrorCode.PLACE_PICK_CLOSED);
        }
        if (meetingPlacePickRepository.existsByMeetingIdAndMemberIdAndPlaceId(meetingId, memberId, placeId)) {
            return;
        }

        meetingPlacePickRepository.save(MeetingPlacePick.of(meetingId, memberId, placeId));
        checkAndAutoTransitionToVoting(meeting);
    }

    @Transactional
    public void cancelPick(Long meetingId, Long memberId, Long placeId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        if (meeting.getLocationStatus() != LocationStatus.RECOMMENDED) {
            throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
        }
        if (meeting.isPickDeadlineExpired()) {
            throw new BusinessException(ErrorCode.PLACE_PICK_CLOSED);
        }

        meetingPlacePickRepository.deleteByMeetingIdAndMemberIdAndPlaceId(meetingId, memberId, placeId);
    }

    @Transactional(readOnly = true)
    public PickStatusResponse getPickStatus(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
        List<MeetingPlacePick> allPicks = meetingPlacePickRepository.findByMeetingId(meetingId);

        Set<Long> participantMemberIds = participants.stream()
                .map(MeetingParticipant::getMemberId).collect(Collectors.toSet());
        Map<Long, Member> memberById = memberRepository.findAllById(participantMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        Map<Long, Long> pickCountByMemberId = allPicks.stream()
                .collect(Collectors.groupingBy(MeetingPlacePick::getMemberId, Collectors.counting()));

        List<MemberPickStatus> memberStatuses = participants.stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                .map(p -> {
                    Member member = memberById.get(p.getMemberId());
                    String nickname = member != null ? member.getNickname() : "";
                    String profileImageUrl = member != null ? member.getProfileImageUrl() : null;
                    boolean done = pickCountByMemberId.getOrDefault(p.getMemberId(), 0L) >= 1;
                    return new MemberPickStatus(p.getMemberId(), nickname, profileImageUrl, done);
                })
                .toList();

        List<Long> myPicks = allPicks.stream()
                .filter(p -> memberId.equals(p.getMemberId()))
                .map(MeetingPlacePick::getPlaceId).toList();

        return new PickStatusResponse(memberStatuses, myPicks);
    }

    @Transactional
    public void startVoting(Long meetingId, Long memberId, int durationDays) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        GroupMember caller = groupMemberRepository
                .findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        if (meeting.getLocationStatus() != LocationStatus.RECOMMENDED) {
            throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
        }
        if (!meetingPlacePickRepository.existsByMeetingId(meetingId)) {
            throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
        }
        if (!VALID_DURATION_DAYS.contains(durationDays)) {
            throw new BusinessException(ErrorCode.INVALID_DURATION_DAYS);
        }

        LocalDate voteDeadlineDate = LocalDate.now().plusDays(durationDays);
        if (meeting.getConfirmedDate() != null && !voteDeadlineDate.isBefore(meeting.getConfirmedDate())) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_DEADLINE_INVALID);
        }

        meeting.toVoting();
        meetingRepository.save(meeting);
        placeVoteService.createSession(meetingId, durationDays);
    }

    private void checkAndAutoTransitionToVoting(Meeting meeting) {
        List<MeetingParticipant> activeParticipants = meetingParticipantRepository
                .findByMeetingId(meeting.getId()).stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                .toList();

        if (activeParticipants.isEmpty()) return;

        boolean allDone = activeParticipants.stream()
                .allMatch(p -> meetingPlacePickRepository
                        .countByMeetingIdAndMemberId(meeting.getId(), p.getMemberId()) >= 1);

        if (allDone) {
            meeting.toVoting();
            meetingRepository.save(meeting);
            placeVoteService.createSessionWithDefaultDuration(meeting.getId());
        }
    }
}
