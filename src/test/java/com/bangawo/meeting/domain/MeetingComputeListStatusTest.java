package com.bangawo.meeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingComputeListStatusTest {

    private Meeting meetingWith(LocationStatus loc, DateVoteStatus date, LocalDate confirmedDate) {
        return Meeting.builder()
                .id(1L).groupId(1L).name("test").themeTagCode("DINING")
                .locationStatus(loc).dateVoteStatus(date)
                .confirmedDate(confirmedDate != null ? confirmedDate.atStartOfDay() : null)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("confirmedDate가 오늘 이전이면 CLOSED")
    void closedWhenConfirmedDateBeforeToday() {
        Meeting m = meetingWith(LocationStatus.CONFIRMED, DateVoteStatus.COMPLETED,
                LocalDate.now().minusDays(1));
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.CLOSED);
    }

    @Test
    @DisplayName("confirmedDate가 오늘이면 CLOSED 아님")
    void notClosedWhenConfirmedDateIsToday() {
        Meeting m = meetingWith(LocationStatus.CONFIRMED, DateVoteStatus.COMPLETED,
                LocalDate.now());
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirmedDate null이고 날짜 지나도 CLOSED 아님")
    void notClosedWhenConfirmedDateIsNull() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.BEFORE, null);
        assertThat(m.computeListStatus(LocalDate.now().minusDays(30))).isEqualTo(MeetingListStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("locationStatus RECOMMENDED이면 IN_PROGRESS")
    void inProgressWhenLocationRecommended() {
        Meeting m = meetingWith(LocationStatus.RECOMMENDED, DateVoteStatus.BEFORE, null);
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("locationStatus VOTING이면 IN_PROGRESS")
    void inProgressWhenLocationVoting() {
        Meeting m = meetingWith(LocationStatus.VOTING, DateVoteStatus.BEFORE, null);
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("dateVoteStatus IN_PROGRESS이면 IN_PROGRESS")
    void inProgressWhenDateVoteInProgress() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.IN_PROGRESS, null);
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("locationStatus CONFIRMED, dateVoteStatus COMPLETED이면 CONFIRMED")
    void confirmedWhenBothCompleted() {
        Meeting m = meetingWith(LocationStatus.CONFIRMED, DateVoteStatus.COMPLETED, null);
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.CONFIRMED);
    }

    @Test
    @DisplayName("둘 다 BEFORE이면 IN_PROGRESS (초기 상태)")
    void inProgressWhenBothBefore() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.BEFORE, null);
        assertThat(m.computeListStatus(LocalDate.now())).isEqualTo(MeetingListStatus.IN_PROGRESS);
    }
}
