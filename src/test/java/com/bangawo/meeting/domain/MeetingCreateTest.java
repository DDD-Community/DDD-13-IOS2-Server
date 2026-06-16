package com.bangawo.meeting.domain;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeetingCreateTest {

    @Test
    @DisplayName("categoryLabels/vibes/reservable/parking 없이 생성 가능 (모두 선택)")
    void create_withoutOptionalFields() {
        Meeting m = Meeting.create(1L, "팀 회식", "DINING", null, null, null, null);
        assertThat(m.getCategoryLabels()).isNull();
        assertThat(m.getVibes()).isNull();
        assertThat(m.getReservable()).isNull();
        assertThat(m.getParking()).isNull();
        assertThat(m.getLocationStatus()).isEqualTo(LocationStatus.BEFORE);
        assertThat(m.getDateVoteStatus()).isEqualTo(DateVoteStatus.BEFORE);
    }

    @Test
    @DisplayName("유효한 categoryLabels/reservable/parking으로 생성 가능")
    void create_withValidCategoryLabels() {
        Meeting m = Meeting.create(1L, "팀 회식", "DINING", List.of("한식", "주점"), List.of("왁자지껄"), true, null);
        assertThat(m.getCategoryLabels()).containsExactly("한식", "주점");
        assertThat(m.getVibes()).containsExactly("왁자지껄");
        assertThat(m.getReservable()).isTrue();
        assertThat(m.getParking()).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 categoryLabels는 INVALID_INPUT")
    void create_withInvalidCategoryLabel() {
        assertThatThrownBy(() -> Meeting.create(1L, "팀 회식", "DINING", List.of("없는카테고리"), null, null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("vibes는 표준목록 검증 없이 자유롭게 허용 (느슨 검증, U2 이후 보강)")
    void create_vibesNoStrictValidation() {
        Meeting m = Meeting.create(1L, "팀 회식", "DINING", null, List.of("아무거나"), null, null);
        assertThat(m.getVibes()).containsExactly("아무거나");
    }
}
