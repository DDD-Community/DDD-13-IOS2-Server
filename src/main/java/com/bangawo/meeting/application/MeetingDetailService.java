package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.ThemeTag;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.meeting.presentation.dto.MeetingDetailResponse;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingDetailService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final ThemeTagRepository themeTagRepository;

    public MeetingDetailResponse getDetail(Long meetingId, Long memberId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(meeting.getGroupId())
                .stream()
                .sorted(Comparator.comparing(GroupMember::getJoinedAt))
                .toList();

        Set<Long> memberIds = groupMembers.stream()
                .map(GroupMember::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        Map<Long, String> attendanceByMember = meetingParticipantRepository.findByMeetingId(meetingId)
                .stream()
                .collect(Collectors.toMap(MeetingParticipant::getMemberId, MeetingParticipant::getAttendanceStatus));

        Map<Long, List<DeparturePlace>> placesByMember = departurePlaceRepository
                .findAllByMemberIdIn(List.copyOf(memberIds))
                .stream()
                .collect(Collectors.groupingBy(DeparturePlace::getMemberId));

        ThemeTag themeTag = themeTagRepository.findByCode(meeting.getThemeTagCode()).orElse(null);

        List<MeetingDetailResponse.MemberDetailInfo> members = groupMembers.stream()
                .map(gm -> {
                    Member m = memberMap.get(gm.getMemberId());
                    boolean active = m != null && m.isActive();
                    List<MeetingDetailResponse.DeparturePlaceInfo> places = placesByMember
                            .getOrDefault(gm.getMemberId(), List.of())
                            .stream()
                            .map(p -> new MeetingDetailResponse.DeparturePlaceInfo(
                                    p.getId(),
                                    p.getLabel(),
                                    p.getAddress(),
                                    p.getRoadAddress(),
                                    p.getPlaceName(),
                                    p.getCoordinate().getLatitude(),
                                    p.getCoordinate().getLongitude(),
                                    p.isDefault()
                            ))
                            .toList();

                    return new MeetingDetailResponse.MemberDetailInfo(
                            gm.getMemberId(),
                            active ? m.getNickname() : null,
                            active ? m.getProfileImageUrl() : null,
                            gm.getRole() == GroupMemberRole.HOST,
                            gm.getMemberId().equals(memberId),
                            attendanceByMember.get(gm.getMemberId()),
                            places
                    );
                })
                .toList();

        return new MeetingDetailResponse(
                meeting.getId(),
                meeting.getName(),
                meeting.getThemeTagCode(),
                themeTag != null ? themeTag.getDisplayName() : meeting.getThemeTagCode(),
                meeting.getLocationStatus(),
                meeting.getDateVoteStatus(),
                meeting.getConfirmedDate(),
                members
        );
    }
}
