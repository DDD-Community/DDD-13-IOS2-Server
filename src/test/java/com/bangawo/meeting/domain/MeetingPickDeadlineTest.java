package com.bangawo.meeting.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingPickDeadlineTest {

    private Meeting buildRecommendedMeeting() {
        return Meeting.builder()
                .id(1L).groupId(1L).name("테스트모임").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .build();
    }

    @Test
    void completeRecommendation_pickDeadline_3일_자정으로_설정() {
        Meeting meeting = Meeting.builder()
                .id(1L).groupId(1L).name("테스트모임").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.BEFORE)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .build();

        LocalDateTime before = LocalDateTime.now();
        meeting.completeRecommendation();
        LocalDateTime deadline = meeting.getPickDeadline();

        assertThat(deadline).isNotNull();
        assertThat(deadline.toLocalDate()).isEqualTo(before.plusDays(3).toLocalDate());
        assertThat(deadline.getHour()).isEqualTo(23);
        assertThat(deadline.getMinute()).isEqualTo(59);
        assertThat(deadline.getSecond()).isEqualTo(59);
    }

    @Test
    void isPickDeadlineExpired_마감_전_false() {
        Meeting meeting = buildRecommendedMeeting();
        // pickDeadline = 미래
        Meeting meetingWithDeadline = Meeting.builder()
                .id(1L).groupId(1L).name("테스트모임").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .pickDeadline(LocalDateTime.now().plusDays(1))
                .build();

        assertThat(meetingWithDeadline.isPickDeadlineExpired()).isFalse();
    }

    @Test
    void isPickDeadlineExpired_마감_후_true() {
        Meeting meeting = Meeting.builder()
                .id(1L).groupId(1L).name("테스트모임").themeTagCode("DINING")
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.RECOMMENDED)
                .dateVoteStatus(DateVoteStatus.COMPLETED)
                .pickDeadline(LocalDateTime.now().minusSeconds(1))
                .build();

        assertThat(meeting.isPickDeadlineExpired()).isTrue();
    }

    @Test
    void isPickDeadlineExpired_deadline_null_false() {
        Meeting meeting = buildRecommendedMeeting();
        assertThat(meeting.isPickDeadlineExpired()).isFalse();
    }
}
