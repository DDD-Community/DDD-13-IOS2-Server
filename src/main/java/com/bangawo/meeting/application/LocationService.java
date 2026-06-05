package com.bangawo.meeting.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import com.bangawo.subway.domain.StationCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MidpointCalculationService midpointCalculationService;
    private final MidpointStationCandidateRepository midpointStationCandidateRepository;

    @Transactional
    public void startLocationPhase(Long meetingId, Long requestMemberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        GroupMember caller = groupMemberRepository
                .findByGroupIdAndMemberId(meeting.getGroupId(), requestMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        meeting.startLocationPhase();

        List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetingId(meetingId)
                .stream()
                .filter(p -> !p.getAttendanceStatus().equals(AttendanceStatus.ABSENT.name()))
                .toList();

        boolean anyMissingDeparture = participants.stream().anyMatch(p -> !p.hasCoordinate());
        if (anyMissingDeparture) {
            throw new BusinessException(ErrorCode.PARTICIPANT_DEPARTURE_NOT_SET);
        }

        List<StationCandidate> stations = midpointCalculationService.calculate(meetingId);

        List<MidpointStationCandidate> candidates = new java.util.ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            StationCandidate s = stations.get(i);
            candidates.add(MidpointStationCandidate.of(meetingId, i + 1, s.stationName(), s.lines(), s.distanceKm()));
        }
        midpointStationCandidateRepository.saveAll(candidates);

        meetingRepository.save(meeting);
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
