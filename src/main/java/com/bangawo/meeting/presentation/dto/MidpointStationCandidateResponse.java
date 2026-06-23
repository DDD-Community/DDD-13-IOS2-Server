package com.bangawo.meeting.presentation.dto;

import com.bangawo.meeting.domain.MidpointStationCandidate;

import java.util.List;

public record MidpointStationCandidateResponse(List<StationInfo> candidates) {

    public record StationInfo(int rank, Long stationId, String stationName, String lines,
                              double distanceKm, double latitude, double longitude) {
        public static StationInfo from(MidpointStationCandidate domain) {
            return new StationInfo(
                    domain.getRank(),
                    domain.getStationId(),
                    domain.getStationName(),
                    domain.getLines(),
                    domain.getDistanceKm(),
                    domain.getLatitude(),
                    domain.getLongitude()
            );
        }
    }

    public static MidpointStationCandidateResponse from(List<MidpointStationCandidate> candidates) {
        return new MidpointStationCandidateResponse(
                candidates.stream().map(StationInfo::from).toList()
        );
    }
}
