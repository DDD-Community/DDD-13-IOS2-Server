package com.bangawo.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다"),
    SOCIAL_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_004", "소셜 인증에 실패했습니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다"),
    NICKNAME_FORBIDDEN_WORD(HttpStatus.BAD_REQUEST, "MEMBER_002", "사용할 수 없는 닉네임입니다"),
    DEPARTURE_PLACE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEMBER_003", "출발지는 최대 10개까지 등록 가능합니다"),
    DEFAULT_DEPARTURE_PLACE_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "MEMBER_004", "기본 출발지는 삭제할 수 없습니다"),
    DEPARTURE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_005", "출발지를 찾을 수 없습니다"),

    // Terms
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_001", "필수 약관에 동의해야 합니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
