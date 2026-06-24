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
    private static final int MIN_CANDIDATES = 3;
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
    private final MemberRepository memberRepository;

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
        backfillCandidatesIfNeeded(meetingId);
        MeetingPlaceVoteSession session = MeetingPlaceVoteSession.create(meetingId, durationDays);
        MeetingPlaceVoteSession saved = voteSessionRepository.save(session);
        computeAndSaveTravelBurdens(meetingId, saved.getId());
        return saved;
    }

    @Transactional
    public MeetingPlaceVoteSession createSessionWithDefaultDuration(Long meetingId) {
        return createSession(meetingId, DEFAULT_DURATION_DAYS);
    }

    /**
     * 투표 후보(담긴 장소 distinct)가 최소치(3) 미만이면 추천을 rank 오름차순으로
     * 시스템 백필한다. 추천 총량이 부족하면 가능한 만큼만. 모든 세션 생성 경로의 단일 진입점.
     */
    private void backfillCandidatesIfNeeded(Long meetingId) {
        List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
        Set<Long> candidatePlaceIds = picks.stream()
                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (candidatePlaceIds.size() >= MIN_CANDIDATES) {
            return;
        }

        List<MeetingPlacePick> backfills = new ArrayList<>();
        for (MeetingPlaceRecommendation rec : recommendationRepository.findByMeetingIdOrderByRank(meetingId)) {
            if (candidatePlaceIds.contains(rec.getPlaceId())) {
                continue;
            }
            backfills.add(MeetingPlacePick.ofSystem(meetingId, rec.getPlaceId()));
            candidatePlaceIds.add(rec.getPlaceId());
            if (candidatePlaceIds.size() >= MIN_CANDIDATES) {
                break;
            }
        }
        if (!backfills.isEmpty()) {
            meetingPlacePickRepository.saveAll(backfills);
            log.info("후보 백필 meetingId={} 추가={}건", meetingId, backfills.size());
        }
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

        Set<Long> candidatePlaceIds = meetingPlacePickRepository.findByMeetingId(meetingId).stream()
                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toSet());
        int maxVotes = Math.max(1, candidatePlaceIds.size() / 2);

        if (placeIds.size() > maxVotes) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_LIMIT_EXCEEDED);
        }
        if (!candidatePlaceIds.containsAll(placeIds)) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_INVALID_CANDIDATE);
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

        List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
        List<Long> candidatePlaceIds = picks.stream()
                .map(MeetingPlacePick::getPlaceId).distinct().toList();

        List<MeetingPlaceVote> allVotes = voteRepository.findBySessionId(session.getId());
        Map<Long, Long> voteCountByPlaceId = allVotes.stream()
                .collect(Collectors.groupingBy(MeetingPlaceVote::getPlaceId, Collectors.counting()));
        Set<Long> myVotedPlaceIds = allVotes.stream()
                .filter(v -> memberId.equals(v.getMemberId()))
                .map(MeetingPlaceVote::getPlaceId).collect(Collectors.toSet());

        List<MeetingTravelBurden> allBurdens = travelBurdenRepository.findByMeetingId(meetingId);
        Map<Long, List<MeetingTravelBurden>> burdensByPlaceId = allBurdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId));

        Map<Long, Place> placeById = placeRepository.findByIds(candidatePlaceIds)
                .stream().collect(Collectors.toMap(Place::getId, p -> p));

        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId);
        Set<Long> voterIds = allVotes.stream().map(MeetingPlaceVote::getMemberId).collect(Collectors.toSet());
        List<MeetingParticipant> activeParticipants = participants.stream()
                .filter(p -> !"ABSENT".equals(p.getAttendanceStatus())).toList();
        Set<Long> activeIds = activeParticipants.stream()
                .map(MeetingParticipant::getMemberId).collect(Collectors.toSet());
        int totalActive = activeIds.size();
        int votedCount = (int) activeIds.stream().filter(voterIds::contains).count();

        // 정렬: 내가 미투표면 가나다순, 투표했으면 득표순(동점 시 가나다순)
        boolean voted = !myVotedPlaceIds.isEmpty();
        Comparator<Long> nameAsc = Comparator.comparing(
                id -> placeById.get(id) != null ? placeById.get(id).getName() : "");
        Comparator<Long> ordering = voted
                ? Comparator.comparingLong((Long id) -> voteCountByPlaceId.getOrDefault(id, 0L))
                        .reversed().thenComparing(nameAsc)
                : nameAsc;
        List<Long> sortedPlaceIds = candidatePlaceIds.stream().sorted(ordering).toList();

        List<PlaceVoteStatusResponse.CandidateVoteInfo> candidates = sortedPlaceIds.stream()
                .map(placeId -> {
                    List<MeetingTravelBurden> burdens =
                            burdensByPlaceId.getOrDefault(placeId, List.of());
                    long maxSec = burdens.stream()
                            .mapToLong(MeetingTravelBurden::getSeconds).max().orElse(0);
                    List<PlaceVoteStatusResponse.MemberBurdenInfo> burdenInfos = burdens.stream()
                            .map(b -> new PlaceVoteStatusResponse.MemberBurdenInfo(
                                    b.getMemberId(), b.getSeconds(), b.getTransfers(),
                                    b.getSeconds() == maxSec))
                            .toList();
                    return new PlaceVoteStatusResponse.CandidateVoteInfo(
                            PlaceSummary.from(placeById.get(placeId)),
                            voteCountByPlaceId.getOrDefault(placeId, 0L).intValue(),
                            myVotedPlaceIds.contains(placeId),
                            burdenInfos);
                })
                .toList();

        // 멤버별 참여 현황 — 모든 호출자에게 제공(전원 공개). 활성 참여자 기준, 투표 대상은 비공개.
        Map<Long, Member> memberById = memberRepository.findAllById(activeIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));
        List<PlaceVoteStatusResponse.MemberVoteStatus> memberStatuses = activeParticipants.stream()
                .map(p -> {
                    Member m = memberById.get(p.getMemberId());
                    String name = m != null ? m.getNickname() : "";
                    return new PlaceVoteStatusResponse.MemberVoteStatus(
                            p.getMemberId(), name, voterIds.contains(p.getMemberId()));
                })
                .toList();

        return new PlaceVoteStatusResponse(session.getDeadline(), session.getStatus(),
                totalActive, votedCount, memberStatuses, candidates);
    }

    @Transactional(readOnly = true)
    public PlaceTravelBurdenResponse getPlaceTravelBurden(Long meetingId, Long placeId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<MeetingTravelBurden> burdens = travelBurdenRepository.findByMeetingIdAndPlaceId(meetingId, placeId);
        long maxSec = burdens.stream().mapToLong(MeetingTravelBurden::getSeconds).max().orElse(0);

        Set<Long> burdenMemberIds = burdens.stream()
                .map(MeetingTravelBurden::getMemberId).collect(Collectors.toSet());
        Map<Long, Member> memberById = memberRepository.findAllById(burdenMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        Place place = placeRepository.findByIds(List.of(placeId)).stream().findFirst().orElse(null);

        List<PlaceTravelBurdenResponse.MemberBurden> memberBurdens = burdens.stream()
                .map(b -> {
                    Member m = memberById.get(b.getMemberId());
                    String name = m != null ? m.getNickname() : "";
                    return new PlaceTravelBurdenResponse.MemberBurden(
                            b.getMemberId(), name, b.getSeconds(), b.getTransfers(),
                            b.getSeconds() == maxSec);
                })
                .toList();

        return new PlaceTravelBurdenResponse(PlaceSummary.from(place), memberBurdens);
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

        // 후보(담긴 장소+백필) distinct → 추천 테이블에서 최근접역 매핑
        Set<Long> candidatePlaceIds = meetingPlacePickRepository.findByMeetingId(meetingId).stream()
                .map(MeetingPlacePick::getPlaceId).collect(Collectors.toSet());
        Map<Long, Long> nearestStationByPlaceId = recommendationRepository
                .findByMeetingIdOrderByRank(meetingId).stream()
                .filter(r -> candidatePlaceIds.contains(r.getPlaceId()))
                .filter(r -> r.getNearestStationId() != null)
                .collect(Collectors.toMap(MeetingPlaceRecommendation::getPlaceId,
                        MeetingPlaceRecommendation::getNearestStationId, (a, b) -> a));

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

            for (Map.Entry<Long, Long> e : nearestStationByPlaceId.entrySet()) {
                Long placeId = e.getKey();
                Long destStation = e.getValue();
                int[] d = distMap.get(destStation);
                int seconds = d != null ? d[0] : Integer.MAX_VALUE / 2;
                int transfers = d != null ? d[1] : 0;
                burdens.add(MeetingTravelBurden.of(meetingId, participant.getMemberId(),
                        placeId, seconds, transfers));
            }
        }

        if (!burdens.isEmpty()) {
            travelBurdenRepository.saveAll(burdens);
        }
    }
}
