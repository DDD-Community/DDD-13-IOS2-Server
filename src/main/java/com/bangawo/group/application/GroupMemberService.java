package com.bangawo.group.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public void updateAttendance(Long memberId, Long groupId, AttendanceStatus status) {
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        groupMember.updateAttendance(status);
        groupMemberRepository.save(groupMember);
    }
}
