package com.bangawo.group.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.group.presentation.dto.CreateGroupResponse;
import com.bangawo.group.presentation.dto.ThemeTagResponse;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingRepository meetingRepository;
    private final ThemeTagRepository themeTagRepository;
    private final com.bangawo.meeting.domain.MeetingParticipantRepository meetingParticipantRepository;
    private final DeparturePlaceRepository departurePlaceRepository;

    public CreateGroupResponse createGroupWithMeeting(Long memberId, String name, String themeTagCode,
                                                       List<String> categoryLabels, List<String> vibes,
                                                       Boolean reservable, Boolean parking) {
        Group group = groupRepository.save(Group.create(name, themeTagCode));
        Meeting meeting = meetingRepository.save(
                Meeting.create(group.getId(), name, themeTagCode, categoryLabels, vibes, reservable, parking));
        groupMemberRepository.save(GroupMember.createHost(group.getId(), memberId));

        var departure = departurePlaceRepository.findDefaultByMemberId(memberId);
        Double lat = departure.map(d -> d.getCoordinate().getLatitude()).orElse(null);
        Double lng = departure.map(d -> d.getCoordinate().getLongitude()).orElse(null);
        meetingParticipantRepository.save(
                MeetingParticipant.create(meeting.getId(), memberId, lat, lng,
                        com.bangawo.group.domain.AttendanceStatus.JOIN.name())
        );

        return new CreateGroupResponse(group.getId(), meeting.getId(), group.getName(), group.getThemeTagCode());
    }

    public Long createNextMeeting(Long groupId, Long memberId, String name, String themeTagCode,
                                   List<String> categoryLabels, List<String> vibes,
                                   Boolean reservable, Boolean parking) {
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

        Meeting newMeeting = meetingRepository.save(
                Meeting.create(groupId, name, themeTagCode, categoryLabels, vibes, reservable, parking));
        return newMeeting.getId();
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
