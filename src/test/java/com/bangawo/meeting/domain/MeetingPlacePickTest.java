package com.bangawo.meeting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingPlacePickTest {

    @Test
    void of_필드_정상_설정() {
        MeetingPlacePick pick = MeetingPlacePick.of(1L, 2L, 3L);

        assertThat(pick.getMeetingId()).isEqualTo(1L);
        assertThat(pick.getMemberId()).isEqualTo(2L);
        assertThat(pick.getPlaceId()).isEqualTo(3L);
        assertThat(pick.getPickedAt()).isNotNull();
        assertThat(pick.getId()).isNull();
    }
}
