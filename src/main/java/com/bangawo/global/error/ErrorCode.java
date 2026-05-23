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
    DEPARTURE_PLACE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEMBER_003", "출발지는 최대 3개까지 등록 가능합니다"),
    DEFAULT_DEPARTURE_PLACE_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "MEMBER_004", "기본 출발지는 삭제할 수 없습니다"),
    DEPARTURE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_005", "출발지를 찾을 수 없습니다"),

    // Terms
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_001", "필수 약관에 동의해야 합니다"),

    // Group
    GROUP_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "GROUP_001", "그룹명은 30자 이하여야 합니다"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_002", "그룹을 찾을 수 없습니다"),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "GROUP_003", "해당 그룹의 구성원이 아닙니다"),
    NOT_GROUP_HOST(HttpStatus.FORBIDDEN, "GROUP_004", "호스트만 수행할 수 있습니다"),

    // Meeting
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_001", "모임을 찾을 수 없습니다"),
    VOTE_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "MEETING_002", "이미 날짜 투표가 시작되었습니다"),
    VOTE_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "MEETING_003", "날짜 투표가 진행 중이 아닙니다"),
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "MEETING_004", "투표 마감일이 지났습니다"),
    VOTE_OPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEETING_005", "유효하지 않은 투표 옵션입니다"),
    INVALID_CANDIDATE_DATE(HttpStatus.BAD_REQUEST, "MEETING_006", "후보 날짜는 오늘 이후여야 합니다"),
    INVALID_CANDIDATE_COUNT(HttpStatus.BAD_REQUEST, "MEETING_007", "후보 날짜는 1~3개여야 합니다"),
    INVALID_DURATION_DAYS(HttpStatus.BAD_REQUEST, "MEETING_008", "투표 기간은 1, 3, 7일 중 하나여야 합니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
