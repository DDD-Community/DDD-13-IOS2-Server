package com.bangawo.group.application;

import com.bangawo.group.domain.Group;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.group.presentation.dto.CreateGroupResponse;
import com.bangawo.group.presentation.dto.ThemeTagResponse;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
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

    public CreateGroupResponse createGroupWithMeeting(Long memberId, String name, String themeTagCode) {
        Group group = groupRepository.save(Group.create(name, themeTagCode));
        Meeting meeting = meetingRepository.save(Meeting.createFirst(group.getId(), name, themeTagCode));
        groupMemberRepository.save(GroupMember.createHost(group.getId(), memberId));
        return new CreateGroupResponse(group.getId(), meeting.getId(), group.getName(), group.getThemeTagCode());
    }

    @Transactional(readOnly = true)
    public List<ThemeTagResponse> getActiveThemeTags() {
        return themeTagRepository.findAllActive().stream()
                .map(ThemeTagResponse::from)
                .toList();
    }
}
