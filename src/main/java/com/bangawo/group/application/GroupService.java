package com.bangawo.group.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.group.presentation.dto.CreateGroupResponse;
import com.bangawo.group.presentation.dto.GroupMemberResponse;
import com.bangawo.group.presentation.dto.ThemeTagResponse;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingRepository meetingRepository;
    private final ThemeTagRepository themeTagRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final MemberRepository memberRepository;

    public CreateGroupResponse createGroupWithMeeting(Long memberId, String name, String themeTagCode,
                                                       List<String> categoryLabels, List<String> vibes,
                                                       Boolean reservable, Boolean parking) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!member.isRegistered()) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_COMPLETED);
        }

        Group group = groupRepository.save(Group.create(name, themeTagCode));
        Meeting meeting = meetingRepository.save(
                Meeting.create(group.getId(), name, themeTagCode, categoryLabels, vibes, reservable, parking));
        groupMemberRepository.save(GroupMember.createHost(group.getId(), memberId));
        seedParticipant(meeting.getId(), memberId);

        return new CreateGroupResponse(group.getId(), meeting.getId(), group.getName(), group.getThemeTagCode());
    }

    public Long createNextMeeting(Long groupId, Long memberId, String name, String themeTagCode,
                                   List<String> categoryLabels, List<String> vibes,
                                   Boolean reservable, Boolean parking, List<Long> participantMemberIds) {
        GroupMember caller = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        Meeting latest = meetingRepository.findLatestByGroupId(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        if (!latest.isClosed()) {
            throw new BusinessException(ErrorCode.MEETING_NOT_CLOSED);
        }

        Set<Long> groupMemberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getMemberId)
                .collect(Collectors.toSet());

        // 호스트는 항상 포함, 중복 제거, 순서 유지
        Set<Long> participants = new LinkedHashSet<>();
        participants.add(memberId);
        if (participantMemberIds != null) {
            participants.addAll(participantMemberIds);
        }
        // 선택된 멤버는 모두 현재 그룹 구성원이어야 한다
        participants.forEach(id -> {
            if (!groupMemberIds.contains(id)) {
                throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
            }
        });

        Meeting newMeeting = meetingRepository.save(
                Meeting.create(groupId, name, themeTagCode, categoryLabels, vibes, reservable, parking));
        participants.forEach(id -> seedParticipant(newMeeting.getId(), id));
        return newMeeting.getId();
    }

    private void seedParticipant(Long meetingId, Long memberId) {
        var departure = departurePlaceRepository.findDefaultByMemberId(memberId);
        meetingParticipantRepository.save(
                MeetingParticipant.create(
                        meetingId, memberId,
                        departure.map(d -> d.getCoordinate().getLatitude()).orElse(null),
                        departure.map(d -> d.getCoordinate().getLongitude()).orElse(null),
                        AttendanceStatus.JOIN.name(),
                        departure.map(DeparturePlace::getLabel).orElse(null),
                        departure.map(DeparturePlace::getPlaceName).orElse(null),
                        departure.map(DeparturePlace::resolvedAddress).orElse(null)));
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId, Long memberId) {
        groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId).stream()
                .sorted(java.util.Comparator.comparing(GroupMember::getJoinedAt))
                .toList();

        Set<Long> memberIds = members.stream().map(GroupMember::getMemberId).collect(Collectors.toSet());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        return members.stream()
                .map(gm -> {
                    Member m = memberMap.get(gm.getMemberId());
                    boolean active = m != null && m.isActive();
                    return new GroupMemberResponse(
                            gm.getMemberId(),
                            active ? m.getNickname() : null,
                            active ? m.getProfileImageUrl() : null,
                            gm.getRole().name(),
                            gm.getJoinedAt());
                })
                .toList();
    }

    public void closeGroup(Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
        groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .filter(gm -> gm.getRole() == GroupMemberRole.HOST)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_HOST));
        if (!group.isActive()) {
            throw new BusinessException(ErrorCode.GROUP_ALREADY_CLOSED);
        }
        group.close();
        groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<ThemeTagResponse> getActiveThemeTags() {
        return themeTagRepository.findAllActive().stream()
                .map(ThemeTagResponse::from)
                .toList();
    }
}
