package com.bangawo.meeting.domain;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeetingLocationPhaseTest {

    private Meeting meetingWith(LocationStatus loc, DateVoteStatus date) {
        return Meeting.builder()
                .id(1L).groupId(1L).name("test").themeTagCode("DINING")
                .locationStatus(loc).dateVoteStatus(date)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("날짜 미확정이면 assertCanStartLocationPhase는 PLACE_PHASE_NOT_READY")
    void assertCanStart_dateNotCompleted() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.BEFORE);
        assertThatThrownBy(m::assertCanStartLocationPhase)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PLACE_PHASE_NOT_READY);
    }

    @Test
    @DisplayName("locationStatus가 BEFORE가 아니면 assertCanStartLocationPhase는 LOCATION_PHASE_ALREADY_STARTED")
    void assertCanStart_alreadyStarted() {
        Meeting m = meetingWith(LocationStatus.RECOMMENDED, DateVoteStatus.COMPLETED);
        assertThatThrownBy(m::assertCanStartLocationPhase)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOCATION_PHASE_ALREADY_STARTED);
    }

    @Test
    @DisplayName("날짜 확정 + BEFORE면 assertCanStartLocationPhase는 예외 없이 통과(상태 변경 없음)")
    void assertCanStart_passes() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.COMPLETED);
        m.assertCanStartLocationPhase();
        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.BEFORE);
    }

    @Test
    @DisplayName("completeRecommendation은 BEFORE -> RECOMMENDED")
    void completeRecommendation_transitions() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.COMPLETED);
        m.completeRecommendation();
        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.RECOMMENDED);
    }

    @Test
    @DisplayName("toVoting은 RECOMMENDED -> VOTING")
    void toVoting_transitions() {
        Meeting m = meetingWith(LocationStatus.RECOMMENDED, DateVoteStatus.COMPLETED);
        m.toVoting();
        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.VOTING);
    }

    @Test
    @DisplayName("RECOMMENDED가 아닐 때 toVoting은 LOCATION_NOT_RECOMMENDED")
    void toVoting_invalidState() {
        Meeting m = meetingWith(LocationStatus.BEFORE, DateVoteStatus.COMPLETED);
        assertThatThrownBy(m::toVoting)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOCATION_NOT_RECOMMENDED);
    }

    @Test
    @DisplayName("toConfirmed는 VOTING -> CONFIRMED")
    void toConfirmed_transitions() {
        Meeting m = meetingWith(LocationStatus.VOTING, DateVoteStatus.COMPLETED);
        m.toConfirmed();
        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("VOTING이 아닐 때 toConfirmed는 PLACE_VOTE_NOT_IN_PROGRESS")
    void toConfirmed_invalidState() {
        Meeting m = meetingWith(LocationStatus.RECOMMENDED, DateVoteStatus.COMPLETED);
        assertThatThrownBy(m::toConfirmed)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
    }
}
