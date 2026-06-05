package com.bangawo.member.application;

import com.bangawo.global.common.Coordinate;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeparturePlaceServiceTest {

    @Mock DeparturePlaceRepository repository;
    @InjectMocks DeparturePlaceService service;

    @Test
    void 첫_등록_시_isDefault_강제_true() {
        when(repository.countByMemberId(1L)).thenReturn(0);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(1L, "집", "서울시 강남구", "강남구 테헤란로 1", null, 37.5, 127.0, false);

        ArgumentCaptor<DeparturePlace> captor = ArgumentCaptor.forClass(DeparturePlace.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isDefault()).isTrue();
        verify(repository, never()).clearDefaultByMemberId(1L);
    }

    @Test
    void 최대_3개_초과_시_LIMIT_EXCEEDED_예외() {
        when(repository.countByMemberId(1L)).thenReturn(3);

        assertThatThrownBy(() -> service.create(1L, "집", "서울시", "서울 도로명", null, 37.5, 127.0, false))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DEPARTURE_PLACE_LIMIT_EXCEEDED));
    }

    @Test
    void isDefault_true_요청_시_기존_기본_해제() {
        when(repository.countByMemberId(1L)).thenReturn(1);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(1L, "회사", "서울시 중구", "중구 세종대로 1", null, 37.5, 127.0, true);

        verify(repository).clearDefaultByMemberId(1L);
        ArgumentCaptor<DeparturePlace> captor = ArgumentCaptor.forClass(DeparturePlace.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isDefault()).isTrue();
    }

    @Test
    void 출발지_수정_성공() {
        DeparturePlace existing = DeparturePlace.builder()
                .id(10L).memberId(1L).label("집").address("구주소")
                .roadAddress("구 도로명주소").placeName(null)
                .coordinate(new Coordinate(37.5, 127.0)).isDefault(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(repository.findByIdAndMemberId(10L, 1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(1L, 10L, "회사", "신주소", "신 도로명주소", null, 37.6, 127.1);

        ArgumentCaptor<DeparturePlace> captor = ArgumentCaptor.forClass(DeparturePlace.class);
        verify(repository).save(captor.capture());
        DeparturePlace updated = captor.getValue();
        assertThat(updated.getLabel()).isEqualTo("회사");
        assertThat(updated.getAddress()).isEqualTo("신주소");
        assertThat(updated.isDefault()).isTrue();
    }

    @Test
    void 타인_소유_출발지_수정_시_NOT_FOUND_예외() {
        when(repository.findByIdAndMemberId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, 10L, "회사", "서울", "서울 도로명", null, 37.5, 127.0))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DEPARTURE_PLACE_NOT_FOUND));
    }
}
