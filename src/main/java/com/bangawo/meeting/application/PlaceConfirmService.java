package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.meeting.domain.*;
import com.bangawo.place.domain.Place;
import com.bangawo.place.domain.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceConfirmService {

    private final MeetingRepository meetingRepository;
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
                .map(MeetingPlacePick::getPlaceId)
                .distinct()
                .toList();

        Map<Long, Long> pickOrderByPlaceId = new LinkedHashMap<>();
        long order = 0;
        for (MeetingPlacePick pick : picks) {
            pickOrderByPlaceId.putIfAbsent(pick.getPlaceId(), order++);
        }

        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId).orElse(null);
        Map<Long, Long> voteCountByPlaceId = new HashMap<>();
        if (session != null) {
            List<MeetingPlaceVote> votes = voteRepository.findBySessionId(session.getId());
            voteCountByPlaceId = votes.stream()
                    .collect(Collectors.groupingBy(MeetingPlaceVote::getPlaceId, Collectors.counting()));
        }

        List<MeetingTravelBurden> burdens = travelBurdenRepository.findByMeetingId(meetingId);
        Map<Long, Long> secondsSumByPlaceId = burdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getSeconds)));
        Map<Long, Long> transfersSumByPlaceId = burdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getTransfers)));

        final Map<Long, Long> finalVoteCount = voteCountByPlaceId;
        Long winnerId = candidatePlaceIds.stream()
                .max(Comparator
                        .comparingLong((Long id) -> finalVoteCount.getOrDefault(id, 0L))
                        .thenComparingLong(id -> -secondsSumByPlaceId.getOrDefault(id, 0L))
                        .thenComparingLong(id -> -transfersSumByPlaceId.getOrDefault(id, 0L))
                        .thenComparingLong(id -> -pickOrderByPlaceId.getOrDefault(id, Long.MAX_VALUE)))
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

    @Transactional(readOnly = true)
    public com.bangawo.meeting.presentation.dto.PlaceResultResponse getResult(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        if (meeting.getLocationStatus() != LocationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED);
        }

        MeetingConfirmedPlace confirmed = confirmedPlaceRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_CONFIRMED));

        List<MeetingPlacePick> picks = meetingPlacePickRepository.findByMeetingId(meetingId);
        List<Long> candidateIds = picks.stream().map(MeetingPlacePick::getPlaceId).distinct().toList();

        MeetingPlaceVoteSession session = voteSessionRepository.findByMeetingId(meetingId).orElse(null);
        Map<Long, Long> voteCount = new HashMap<>();
        if (session != null) {
            voteRepository.findBySessionId(session.getId()).forEach(v ->
                    voteCount.merge(v.getPlaceId(), 1L, Long::sum));
        }

        List<MeetingTravelBurden> burdens = travelBurdenRepository.findByMeetingId(meetingId);
        Map<Long, Long> secondsSum = burdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getSeconds)));
        Map<Long, Long> transfersSum = burdens.stream()
                .collect(Collectors.groupingBy(MeetingTravelBurden::getPlaceId,
                        Collectors.summingLong(MeetingTravelBurden::getTransfers)));

        List<com.bangawo.meeting.presentation.dto.PlaceResultResponse.CandidateResult> candidates =
                candidateIds.stream()
                        .map(id -> new com.bangawo.meeting.presentation.dto.PlaceResultResponse.CandidateResult(
                                id,
                                voteCount.getOrDefault(id, 0L).intValue(),
                                secondsSum.getOrDefault(id, 0L),
                                transfersSum.getOrDefault(id, 0L)))
                        .toList();

        return new com.bangawo.meeting.presentation.dto.PlaceResultResponse(
                confirmed.getPlaceId(), confirmed.getPlaceName(),
                confirmed.getAddress(), confirmed.getConfirmedAt(), candidates);
    }
}
