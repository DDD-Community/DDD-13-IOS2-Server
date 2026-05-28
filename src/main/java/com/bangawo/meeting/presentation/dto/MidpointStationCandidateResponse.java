package com.bangawo.meeting.presentation.dto;

import com.bangawo.meeting.domain.MidpointStationCandidate;

import java.util.List;

public record MidpointStationCandidateResponse(List<StationInfo> candidates) {

    public record StationInfo(int rank, String stationName, String lines, double distanceKm) {
        public static StationInfo from(MidpointStationCandidate domain) {
            return new StationInfo(
                    domain.getRank(),
                    domain.getStationName(),
                    domain.getLines(),
                    domain.getDistanceKm()
            );
        }
    }

    public static MidpointStationCandidateResponse from(List<MidpointStationCandidate> candidates) {
        return new MidpointStationCandidateResponse(
                candidates.stream().map(StationInfo::from).toList()
        );
    }
}
