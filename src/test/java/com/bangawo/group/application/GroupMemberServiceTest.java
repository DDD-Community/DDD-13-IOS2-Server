package com.bangawo.group.application;

import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.AttendanceStatus;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceTest {

    @Mock GroupMemberRepository groupMemberRepository;
    @InjectMocks GroupMemberService groupMemberService;

    @Test
    void 참석여부_수정_성공() {
        GroupMember groupMember = GroupMember.builder()
                .id(1L).groupId(10L).memberId(20L)
                .role(GroupMemberRole.MEMBER)
                .attendanceStatus(AttendanceStatus.JOIN)
                .joinedAt(LocalDateTime.now())
                .build();
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.of(groupMember));
        when(groupMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        groupMemberService.updateAttendance(20L, 10L, AttendanceStatus.LATE);

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getAttendanceStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    void 그룹_미구성원이면_NOT_GROUP_MEMBER_예외() {
        when(groupMemberRepository.findByGroupIdAndMemberId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupMemberService.updateAttendance(20L, 10L, AttendanceStatus.LATE))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_GROUP_MEMBER));
    }
}
