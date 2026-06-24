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
import com.bangawo.place.presentation.dto.PlaceSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceConfirmService {

    private static final int MAX_RANK = 3;

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingPlacePickRepository meetingPlacePickRepository;
    private final MeetingPlaceVoteRepository voteRepository;
    private final MeetingPlaceVoteSessionRepository voteSessionRepository;
    private final MeetingTravelBurdenRepository travelBurdenRepository;
    private final MeetingConfirmedPlaceRepository confirmedPlaceRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public MeetingConfirmedPlace confirmPlace(Long meetingId) {
        List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
        if (picks.isEmpty()) {
            throw new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED);
        }

        List<Long> candidatePlaceIds = picks.stream()
                .map(MeetingPlacePick::getPlaceId).distinct().toList();

        Comparator<Long> comparator = buildCandidateComparator(meetingId, picks);
        Long winnerId = candidatePlaceIds.stream()
                .min(comparator)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED));

        Place place = placeRepository.findByIds(List.of(winnerId)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED));

        MeetingConfirmedPlace confirmed = MeetingConfirmedPlace.of(
                meetingId, winnerId, place.getName(), place.getAddress());
        MeetingConfirmedPlace saved = confirmedPlaceRepository.save(confirmed);
        log.info("장소 확정 meetingId={} placeId={} placeName={}", meetingId, winnerId, place.getName());
        return saved;
    }

    @Transactional
    public MeetingConfirmedPlace confirmByHost(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        GroupMember caller = groupMemberRepository
                .findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        if (meeting.getLocationStatus() != LocationStatus.VOTING) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
        }

        meeting.toConfirmed();
        meetingRepository.save(meeting);
        return confirmPlace(meetingId);
    }

    @Transactional(readOnly = true)
    public PlaceResultResponse getResult(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        if (meeting.getLocationStatus() != LocationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED);
        }

        MeetingConfirmedPlace confirmed = confirmedPlaceRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED));

        List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
        List<Long> candidateIds = picks.stream()
                .map(MeetingPlacePick::getPlaceId).distinct().toList();

        Map<Long, Long> voteCount = voteCountByPlaceId(meetingId);
        Map<Long, Long> secondsSum = secondsSumByPlaceId(meetingId);
        Map<Long, Long> transfersSum = transfersSumByPlaceId(meetingId);

        Map<Long, Place> placeById = placeRepository.findByIds(candidateIds).stream()
                .collect(Collectors.toMap(Place::getId, p -> p));

        // 공통 비교자로 정렬 → 상위 3개에 rank 1·2·3, 나머지는 0 (후보<3이면 후보 수만큼)
        Comparator<Long> comparator = buildCandidateComparator(meetingId, picks);
        List<Long> ranked = candidateIds.stream().sorted(comparator).toList();

        List<PlaceResultResponse.CandidateResult> candidates = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            Long id = ranked.get(i);
            int rank = i < MAX_RANK ? i + 1 : 0;
            candidates.add(new PlaceResultResponse.CandidateResult(
                    rank,
                    PlaceSummary.from(placeById.get(id)),
                    voteCount.getOrDefault(id, 0L).intValue(),
                    secondsSum.getOrDefault(id, 0L),
                    transfersSum.getOrDefault(id, 0L)));
        }

        return new PlaceResultResponse(
                PlaceSummary.from(placeById.get(confirmed.getPlaceId())),
                confirmed.getConfirmedAt(), candidates);
    }

    /**
     * 공통 순위 비교자 (1위가 min): 득표↓ → 이동시간합↑ → 환승합↑ → 최초 담은 시각↑.
     * confirmPlace(1위 선정)와 getResult(1~3위)가 공유하여 결정적 순위를 보장한다.
     */
    private Comparator<Long> buildCandidateComparator(Long meetingId, List<MeetingPlacePick> picks) {
        Map<Long, Long> voteCount = voteCountByPlaceId(meetingId);
        Map<Long, Long> secondsSum = secondsSumByPlaceId(meetingId);
        Map<Long, Long> transfersSum = transfersSumByPlaceId(meetingId);
        Map<Long, LocalDateTime> firstPickedAt = picks.stream().collect(Collectors.toMap(
                MeetingPlacePick::getPlaceId, MeetingPlacePick::getPickedAt,
                (a, b) -> a.isBefore(b) ? a : b));

        return Comparator
                .comparingLong((Long id) -> voteCount.getOrDefault(id, 0L)).reversed()
                .thenComparingLong(id -> secondsSum.getOrDefault(id, 0L))
                .thenComparingLong(id -> transfersSum.getOrDefault(id, 0L))
                .thenComparing(id -> firstPickedAt.getOrDefault(id, LocalDateTime.MAX));
    }

    private Map<Long, Long> voteCountByPlaceId(Long meetingId) {
        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId).orElse(null);
        if (session == null) {
            return Map.of();
        }
        return voteRepository.findBySessionId(session.getId()).stream()
                .collect(Collectors.groupingBy(MeetingPlaceVote::getPlaceId, Collectors.counting()));
    }

    private Map<Long, Long> secondsSumByPlaceId(Long meetingId) {
        return travelBurdenRepository.findByMeetingId(meetingId).stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getSeconds)));
    }

    private Map<Long, Long> transfersSumByPlaceId(Long meetingId) {
        return travelBurdenRepository.findByMeetingId(meetingId).stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getTransfers)));
    }
}
