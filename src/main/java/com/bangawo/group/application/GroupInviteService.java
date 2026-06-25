package com.bangawo.group.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.*;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingParticipant;
import com.bangawo.meeting.domain.MeetingParticipantRepository;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.member.domain.departure.DeparturePlace;
import com.bangawo.member.domain.departure.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupInviteService {

    private static final int INVITE_EXPIRY_HOURS = 48;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final MemberRepository memberRepository;

    public String issueInviteCode(Long groupId, Long requestMemberId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        GroupMember caller = groupMemberRepository.findByGroupIdAndMemberId(groupId, requestMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }

        groupInviteRepository.deleteByGroupId(groupId);

        GroupInvite invite = GroupInvite.create(
                groupId,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(INVITE_EXPIRY_HOURS)
        );
        return groupInviteRepository.save(invite).getCode();
    }

    public Long joinGroup(String inviteCode, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!member.isRegistered()) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_COMPLETED);
        }

        GroupInvite invite = groupInviteRepository.findByCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITE_CODE_NOT_FOUND));

        if (invite.isExpired()) {
            throw new BusinessException(ErrorCode.INVITE_CODE_EXPIRED);
        }

        Long groupId = invite.getGroupId();

        if (groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_GROUP_MEMBER);
        }

        groupMemberRepository.save(GroupMember.createMember(groupId, memberId));

        meetingRepository.findLatestByGroupId(groupId).ifPresent(meeting -> {
            if (!meeting.isClosed()) {
                createMeetingParticipant(meeting, memberId);
            }
        });

        return groupId;
    }

    void createMeetingParticipant(Meeting meeting, Long memberId) {
        Optional<DeparturePlace> departure = departurePlaceRepository.findDefaultByMemberId(memberId);
        Double lat = departure.map(d -> d.getCoordinate().getLatitude()).orElse(null);
        Double lng = departure.map(d -> d.getCoordinate().getLongitude()).orElse(null);
        String depLabel = departure.map(DeparturePlace::getLabel).orElse(null);
        String depPlaceName = departure.map(DeparturePlace::getPlaceName).orElse(null);
        String depAddress = departure.map(DeparturePlace::resolvedAddress).orElse(null);

        meetingParticipantRepository.save(
                MeetingParticipant.create(meeting.getId(), memberId, lat, lng, AttendanceStatus.JOIN.name(),
                        depLabel, depPlaceName, depAddress)
        );
    }
}
