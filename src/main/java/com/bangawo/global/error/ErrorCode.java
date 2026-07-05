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
    REGISTRATION_NOT_COMPLETED(HttpStatus.FORBIDDEN, "MEMBER_006", "회원가입(기본 출발지 등록)을 완료해야 모임에 참가할 수 있습니다"),

    // Terms
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_001", "필수 약관에 동의해야 합니다"),

    // Group
    GROUP_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "GROUP_001", "그룹명은 30자 이하여야 합니다"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_002", "그룹을 찾을 수 없습니다"),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "GROUP_003", "해당 그룹의 구성원이 아닙니다"),
    NOT_GROUP_HOST(HttpStatus.FORBIDDEN, "GROUP_004", "호스트만 수행할 수 있습니다"),
    GROUP_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "GROUP_005", "이미 종료된 그룹입니다"),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_006", "유효하지 않은 초대 코드입니다"),
    INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "GROUP_007", "만료된 초대 코드입니다"),
    ALREADY_GROUP_MEMBER(HttpStatus.BAD_REQUEST, "GROUP_008", "이미 이 그룹의 구성원입니다"),

    // Meeting
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_001", "모임을 찾을 수 없습니다"),
    VOTE_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "MEETING_002", "이미 날짜 투표가 시작되었습니다"),
    VOTE_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "MEETING_003", "날짜 투표가 진행 중이 아닙니다"),
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "MEETING_004", "투표 마감일이 지났습니다"),
    VOTE_OPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEETING_005", "유효하지 않은 투표 옵션입니다"),
    INVALID_CANDIDATE_DATE(HttpStatus.BAD_REQUEST, "MEETING_006", "후보 날짜는 오늘 이후여야 합니다"),
    INVALID_CANDIDATE_COUNT(HttpStatus.BAD_REQUEST, "MEETING_007", "후보 날짜는 1~10개여야 합니다"),
    INVALID_DURATION_DAYS(HttpStatus.BAD_REQUEST, "MEETING_008", "투표 기간은 1, 3, 7일 중 하나여야 합니다"),
    MEETING_NOT_CLOSED(HttpStatus.BAD_REQUEST, "MEETING_009", "현재 모임이 종료되지 않아 새 모임을 생성할 수 없습니다"),
    LOCATION_PHASE_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "MEETING_010", "장소 선정이 이미 시작되었습니다"),
    PARTICIPANT_DEPARTURE_NOT_SET(HttpStatus.BAD_REQUEST, "MEETING_011", "출발지를 등록하지 않은 참여자가 있습니다"),
    MIDPOINT_STATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEETING_012", "중간지점 근처에 지하철역을 찾을 수 없습니다"),
    MEETING_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_013", "모임 참여자를 찾을 수 없습니다"),
    PLACE_PHASE_NOT_READY(HttpStatus.BAD_REQUEST, "MEETING_014", "날짜가 아직 확정되지 않았습니다"),
    PLACE_RECOMMENDATION_EMPTY(HttpStatus.BAD_REQUEST, "MEETING_015", "추천 가능한 장소가 없습니다"),
    LOCATION_NOT_RECOMMENDED(HttpStatus.BAD_REQUEST, "MEETING_016", "장소 추천 단계가 아닙니다"),
    PLACE_PICK_CLOSED(HttpStatus.BAD_REQUEST, "MEETING_017", "담기가 마감되었습니다"),
    PLACE_VOTE_DEADLINE_INVALID(HttpStatus.BAD_REQUEST, "MEETING_018", "투표 마감일은 약속 날짜 이전으로 설정해 주세요"),
    PLACE_VOTE_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "MEETING_019", "투표가 진행 중이 아닙니다"),
    PLACE_VOTE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEETING_020", "투표 가능 개수를 초과했습니다"),
    PLACE_NOT_CONFIRMED(HttpStatus.BAD_REQUEST, "MEETING_021", "아직 장소가 확정되지 않았습니다"),
    PLACE_VOTE_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "MEETING_022", "이미 장소 투표가 시작되었습니다"),
    PLACE_VOTE_INVALID_CANDIDATE(HttpStatus.BAD_REQUEST, "MEETING_023", "후보에 없는 장소에 투표할 수 없습니다"),
    NOT_MEETING_PARTICIPANT(HttpStatus.FORBIDDEN, "MEETING_024", "이 모임의 참여자가 아닙니다"),
    ABSENT_PARTICIPANT_CANNOT_ACT(HttpStatus.FORBIDDEN, "MEETING_025", "불참으로 표시한 참여자는 담기·투표에 참여할 수 없습니다"),
    ATTENDANCE_LOCKED(HttpStatus.BAD_REQUEST, "MEETING_026", "장소 선정 단계가 시작되어 참석 여부를 변경할 수 없습니다"),

    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_001", "장소를 찾을 수 없습니다"),

    // Storage
    STORAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "STORAGE_001", "지원하지 않는 이미지 형식입니다"),
    STORAGE_SIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_002", "이미지 업로드 URL 생성에 실패했습니다"),
    STORAGE_INVALID_PATH(HttpStatus.BAD_REQUEST, "STORAGE_003", "올바르지 않은 이미지 경로입니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}