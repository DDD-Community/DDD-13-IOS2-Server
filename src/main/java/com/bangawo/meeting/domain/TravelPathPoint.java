package com.bangawo.meeting.domain;

/**
 * 이동 경로의 한 지점 (지하철 역). 출발역→도착역 경로를 순서대로 구성하는 값객체.
 * meeting_travel_burden.station_path(JSONB)로 직렬화 저장된다.
 */
public record TravelPathPoint(Long stationId, double latitude, double longitude) {
}
