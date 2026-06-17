package com.bangawo.subway.domain;

public record SubwayEdge(
        Long fromStationId,
        Long toStationId,
        int weightSec,
        String edgeType
) {
    public boolean isTransfer() {
        return "TRANSFER".equals(edgeType);
    }
}
