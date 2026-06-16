package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.RecommendationItemResponse;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.domain.PlaceScorer;
import com.bangawo.place.domain.RecommendationCandidate;
import com.bangawo.place.domain.ScoredCandidate;
import com.bangawo.subway.domain.StationCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceSelectionService {

    private static final int RECOMMENDATION_LIMIT = 15;
    private static final double DEFAULT_RADIUS_KM = 2.0;
    private static final double MAX_RADIUS_KM = 6.0;
    private static final double[] RADIUS_RUNGS_KM = {4.0, 6.0};

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MidpointCalculationService midpointCalculationService;
    private final MidpointStationCandidateRepository midpointStationCandidateRepository;
    private final PlaceRepository placeRepository;
    private final MeetingPlaceRecommendationRepository meetingPlaceRecommendationRepository;
    private final ThemeTagRepository themeTagRepository;
    private final PlaceScorer placeScorer = new PlaceScorer();

    @Transactional
    public void startLocationPhase(Long meetingId, Long requestMemberId, Double radiusKm) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        GroupMember caller = groupMemberRepository
                .findByGroupIdAndMemberId(meeting.getGroupId(), requestMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        meeting.assertCanStartLocationPhase();

        if (radiusKm != null && radiusKm > MAX_RADIUS_KM) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId)
                .stream()
                .filter(p -> !p.getAttendanceStatus().equals(AttendanceStatus.ABSENT.name()))
                .toList();

        boolean anyMissingDeparture = participants.stream().anyMatch(p -> !p.hasCoordinate());
        if (anyMissingDeparture) {
            throw new BusinessException(ErrorCode.PARTICIPANT_DEPARTURE_NOT_SET);
        }

        List<StationCandidate> stations = midpointCalculationService.calculate(meetingId);

        List<MidpointStationCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            StationCandidate s = stations.get(i);
            candidates.add(MidpointStationCandidate.of(meetingId, i + 1, s.stationName(), s.lines(), s.distanceKm()));
        }
        midpointStationCandidateRepository.saveAll(candidates);

        List<Long> stationIds = stations.stream().map(StationCandidate::stationId).toList();
        double start = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;
        List<Double> radiusLadderKm = java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(start), java.util.Arrays.stream(RADIUS_RUNGS_KM).boxed())
                .distinct()
                .filter(r -> r >= start)
                .sorted()
                .toList();

        List<RecommendationCandidate> placeCandidates = List.of();
        for (double radiusKmRung : radiusLadderKm) {
            placeCandidates = placeRepository.findCandidates(
                    stationIds, radiusKmRung * 1000, meeting.getReservable(), meeting.getParking());
            if (!placeCandidates.isEmpty()) {
                break;
            }
        }
        if (placeCandidates.isEmpty()) {
            throw new BusinessException(ErrorCode.PLACE_RECOMMENDATION_EMPTY);
        }

        String themeTagDisplayName = themeTagRepository.findByCode(meeting.getThemeTagCode())
                .map(themeTag -> themeTag.getDisplayName())
                .orElse(meeting.getThemeTagCode());
        List<ScoredCandidate> scored = placeScorer.score(
                placeCandidates, themeTagDisplayName, meeting.getCategoryLabels(), meeting.getVibes());
        List<ScoredCandidate> top = scored.stream()
                .sorted(Comparator.comparingDouble(ScoredCandidate::score).reversed())
                .limit(RECOMMENDATION_LIMIT)
                .toList();

        List<MeetingPlaceRecommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            ScoredCandidate s = top.get(i);
            recommendations.add(MeetingPlaceRecommendation.of(
                    meetingId,
                    s.candidate().place().getId(),
                    i + 1,
                    s.score(),
                    s.candidate().nearestStationId()
            ));
        }
        meetingPlaceRecommendationRepository.saveAll(recommendations);

        meeting.completeRecommendation();
        meetingRepository.save(meeting);
    }

    @Transactional(readOnly = true)
    public List<RecommendationItemResponse> getRecommendations(Long meetingId, Long requestMemberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), requestMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<MeetingPlaceRecommendation> recommendations =
                meetingPlaceRecommendationRepository.findByMeetingIdOrderByRank(meetingId);

        Map<Long, Place> placeById = placeRepository.findByIds(
                        recommendations.stream().map(MeetingPlaceRecommendation::getPlaceId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(Place::getId, p -> p));

        return recommendations.stream()
                .map(r -> {
                    Place place = placeById.get(r.getPlaceId());
                    return new RecommendationItemResponse(
                            r.getRank(),
                            r.getPlaceId(),
                            place != null ? place.getName() : null,
                            place != null ? place.getCategoryLabel() : null,
                            r.getScore(),
                            r.getNearestStationId()
                    );
                })
                .toList();
    }

    @Transactional
    public void updateParticipantDeparture(Long meetingId, Long memberId, Long departurePlaceId) {
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        DeparturePlace departure = departurePlaceRepository.findByIdAndMemberId(departurePlaceId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTURE_PLACE_NOT_FOUND));

        MeetingParticipant participant = meetingParticipantRepository
                .findByMeetingIdAndMemberId(meetingId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND));

        participant.updateDeparture(
                departure.getCoordinate().getLatitude(),
                departure.getCoordinate().getLongitude()
        );
        meetingParticipantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<MidpointStationCandidate> getMidpointStations(Long meetingId, Long requestMemberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), requestMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        return midpointStationCandidateRepository.findByMeetingIdOrderByRank(meetingId);
    }
}
