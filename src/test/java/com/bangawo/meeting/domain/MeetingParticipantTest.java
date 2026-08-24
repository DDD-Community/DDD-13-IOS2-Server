package com.bangawo.meeting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingParticipantTest {

    @Test
    void clearDeparture_좌표와_출발지_메타데이터를_모두_제거한다() {
        MeetingParticipant participant = MeetingParticipant.create(
                1L, 10L, 37.5, 127.0, "PRESENT",
                "집", "카카오장소", "서울시 어딘가");

        participant.clearDeparture();

        assertThat(participant.getLatitude()).isNull();
        assertThat(participant.getLongitude()).isNull();
        assertThat(participant.getDepartureLabel()).isNull();
        assertThat(participant.getDeparturePlaceName()).isNull();
        assertThat(participant.getDepartureAddress()).isNull();
    }

    @Test
    void clearDeparture_이후에도_참여_이력_필드는_유지된다() {
        MeetingParticipant participant = MeetingParticipant.create(
                1L, 10L, 37.5, 127.0, "PRESENT",
                "집", "카카오장소", "서울시 어딘가");

        participant.clearDeparture();

        assertThat(participant.getMeetingId()).isEqualTo(1L);
        assertThat(participant.getMemberId()).isEqualTo(10L);
        assertThat(participant.getAttendanceStatus()).isEqualTo("PRESENT");
        assertThat(participant.hasCoordinate()).isFalse();
    }
}
