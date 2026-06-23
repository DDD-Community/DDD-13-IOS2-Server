package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.PlaceVoteStatusResponse;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import com.bangawo.place.presentation.dto.PlaceSummary;
import com.bangawo.subway.domain.SubwayGraph;
import com.bangawo.subway.domain.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceVoteService {

    private static final int DEFAULT_DURATION_DAYS = 3;
    private static final Set<Integer> VALID_DURATION_DAYS = Set.of(1, 3, 7);

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingPlacePickRepository meetingPlacePickRepository;
    private final MeetingPlaceVoteSessionRepository voteSessionRepository;
    private final MeetingPlaceVoteRepository voteRepository;
    private final MeetingTravelBurdenRepository travelBurdenRepository;
    private final MeetingPlaceRecommendationRepository recommendationRepository;
    private final SubwayGraph subwayGraph;
    private final SubwayStationRepository subwayStationRepository;
    private final PlaceConfirmService placeConfirmService;
    private final PlaceRepository placeRepository;

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
        if (voteSessionRepository.findByMeetingId(meetingId).isPresent()) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_ALREADY_STARTED);
        }
        if (!VALID_DURATION_DAYS.contains(durationDays)) {
            throw new BusinessException(ErrorCode.INVALID_DURATION_DAYS);
        }

        LocalDate voteDeadlineDate = LocalDate.now().plusDays(durationDays);
        if (meeting.getConfirmedDate() != null
                && !voteDeadlineDate.isBefore(meeting.getConfirmedDate().toLocalDate())) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_DEADLINE_INVALID);
        }

        meeting.toVoting();
        meetingRepository.save(meeting);
        createSession(meetingId, durationDays);
    }

    @Transactional
    public MeetingPlaceVoteSession createSession(Long meetingId, int durationDays) {
        MeetingPlaceVoteSession session = MeetingPlaceVoteSession.create(meetingId, durationDays);
        MeetingPlaceVoteSession saved = voteSessionRepository.save(session);
        computeAndSaveTravelBurdens(meetingId, saved.getId());
        return saved;
    }

    @Transactional
    public MeetingPlaceVoteSession createSessionWithDefaultDuration(Long meetingId) {
        return createSession(meetingId, DEFAULT_DURATION_DAYS);
    }

    @Transactional
    public List<MeetingPlaceVote> submitVote(Long meetingId, Long memberId, List<Long> placeIds) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        if (meeting.getLocationStatus() != LocationStatus.VOTING) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
        }

        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS));

        if ("CLOSED".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
        }

        List<MeetingPlaceRecommendation> recommendations =
                recommendationRepository.findByMeetingIdOrderByRank(meetingId);
        int candidateCount = recommendations.size();
        int maxVotes = Math.max(1, candidateCount / 2);

        if (placeIds.size() > maxVotes) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_LIMIT_EXCEEDED);
        }

        voteRepository.deleteBySessionIdAndMemberId(session.getId(), memberId);

        List<MeetingPlaceVote> votes = placeIds.stream()
                .map(placeId -> MeetingPlaceVote.of(session.getId(), memberId, placeId))
                .toList();

        if (!votes.isEmpty()) {
            voteRepository.saveAll(votes);
        }

        if (isAllVotedInternal(meetingId, session.getId())) {
            meeting.toConfirmed();
            meetingRepository.save(meeting);
            placeConfirmService.confirmPlace(meetingId);
        }

        return votes;
    }

    @Transactional(readOnly = true)
    public PlaceVoteStatusResponse getVoteStatus(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        if (meeting.getLocationStatus() != LocationStatus.VOTING) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
        }

        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS));

        List<MeetingPlaceRecommendation> recommendations =
                recommendationRepository.findByMeetingIdOrderByRank(meetingId);
        List<MeetingPlaceVote> allVotes = voteRepository.findBySessionId(session.getId());

        Map<Long, Long> voteCountByPlaceId = allVotes.stream()
                .collect(Collectors.groupingBy(MeetingPlaceVote::getPlaceId, Collectors.counting()));
        Set<Long> myVotedPlaceIds = allVotes.stream()
                .filter(v -> memberId.equals(v.getMemberId()))
                .map(MeetingPlaceVote::getPlaceId).collect(Collectors.toSet());

        List<MeetingTravelBurden> allBurdens = travelBurdenRepository.findByMeetingId(meetingId);
        Map<Long, List<MeetingTravelBurden>> burdensByPlaceId = allBurdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId));

        Map<Long, Place> placeById = placeRepository.findByIds(
                        recommendations.stream().map(MeetingPlaceRecommendation::getPlaceId).toList())
                .stream().collect(Collectors.toMap(Place::getId, p -> p));

        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
        Set<Long> voterIds = allVotes.stream().map(MeetingPlaceVote::getMemberId).collect(Collectors.toSet());
        Set<Long> activeIds = participants.stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                .map(MeetingParticipant::getMemberId).collect(Collectors.toSet());
        int totalActive = activeIds.size();
        int votedCount = (int) activeIds.stream().filter(voterIds::contains).count();

        List<PlaceVoteStatusResponse.CandidateVoteInfo> candidates = recommendations.stream()
                .map(r -> {
                    List<MeetingTravelBurden> burdens =
                            burdensByPlaceId.getOrDefault(r.getPlaceId(), List.of());
                    long maxSec = burdens.stream()
                            .mapToLong(MeetingTravelBurden::getSeconds).max().orElse(0);
                    List<PlaceVoteStatusResponse.MemberBurdenInfo> burdenInfos = burdens.stream()
                            .map(b -> new PlaceVoteStatusResponse.MemberBurdenInfo(
                                    b.getMemberId(), b.getSeconds(), b.getTransfers(),
                                    b.getSeconds() == maxSec))
                            .toList();
                    return new PlaceVoteStatusResponse.CandidateVoteInfo(
                            PlaceSummary.from(placeById.get(r.getPlaceId())),
                            voteCountByPlaceId.getOrDefault(r.getPlaceId(), 0L).intValue(),
                            myVotedPlaceIds.contains(r.getPlaceId()),
                            burdenInfos);
                })
                .toList();

        return new PlaceVoteStatusResponse(session.getDeadline(), session.getStatus(),
                totalActive, votedCount, candidates);
    }

    public boolean isAllVoted(Long meetingId) {
        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId).orElse(null);
        if (session == null) return false;
        return isAllVotedInternal(meetingId, session.getId());
    }

    private boolean isAllVotedInternal(Long meetingId, Long sessionId) {
        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
        long activeCount = participants.stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus())).count();
        if (activeCount == 0) return false;
        long voterCount = voteRepository.countDistinctVotersBySessionId(sessionId);
        return voterCount >= activeCount;
    }

    private void computeAndSaveTravelBurdens(Long meetingId, Long sessionId) {
        if (!subwayGraph.isLoaded()) {
            log.warn("SubwayGraph 미로드 meetingId={} 이동부담 스냅샷 생략", meetingId);
            return;
        }

        List<MeetingParticipant> participants = meetingParticipantRepository
                .findByMeetingId(meetingId).stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus()))
                .filter(MeetingParticipant::hasCoordinate).toList();

        List<MeetingPlaceRecommendation> recommendations =
                recommendationRepository.findByMeetingIdOrderByRank(meetingId);

        List<MeetingTravelBurden> burdens = new ArrayList<>();

        for (MeetingParticipant participant : participants) {
            Optional<Long> nearestOpt = subwayStationRepository
                    .findNearestStationId(participant.getLatitude(), participant.getLongitude());

            if (nearestOpt.isEmpty()) {
                log.warn("nearest station 없음 memberId={}", participant.getMemberId());
                continue;
            }

            Long sourceStation = nearestOpt.get();
            Map<Long, int[]> distMap = subwayGraph.dijkstra(sourceStation);

            for (MeetingPlaceRecommendation rec : recommendations) {
                Long destStation = rec.getNearestStationId();
                if (destStation == null) continue;
                int[] d = distMap.get(destStation);
                int seconds = d != null ? d[0] : Integer.MAX_VALUE / 2;
                int transfers = d != null ? d[1] : 0;
                burdens.add(MeetingTravelBurden.of(meetingId, participant.getMemberId(),
                        rec.getPlaceId(), seconds, transfers));
            }
        }

        if (!burdens.isEmpty()) {
            travelBurdenRepository.saveAll(burdens);
        }
    }
}
