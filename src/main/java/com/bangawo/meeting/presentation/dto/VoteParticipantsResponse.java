package com.bangawo.meeting.presentation.dto;

import java.util.List;

/**
 * 현재 장소투표 참여중인 팀원 목록 — 활성 참여자(ABSENT 제외)별 이름·프로필·출발지·투표여부.
 */
public record VoteParticipantsResponse(
        List<Participant> participants
) {
    public record Participant(
            Long memberId,
            String name,
            String profileImageUrl,
            String departureName,
            boolean isMe,
            boolean voted
    ) {}
}
