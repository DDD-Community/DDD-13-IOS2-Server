package com.bangawo.global.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException 처리 시 커스텀 에러 응답 반환")
    void handleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("MEMBER_001");
        assertThat(response.getBody().message()).isEqualTo("회원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("미처리 예외는 500 에러 반환")
    void handleUnhandledException() {
        Exception exception = new RuntimeException("unexpected");

        ResponseEntity<ErrorResponse> response = handler.handleException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("COMMON_002");
    }
}
