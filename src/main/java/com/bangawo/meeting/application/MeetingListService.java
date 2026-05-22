package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupRepository;
import com.bangawo.group.domain.ThemeTag;
import com.bangawo.group.domain.ThemeTagRepository;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingRepository;
import com.bangawo.meeting.presentation.dto.MeetingCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingListService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;
    private final ThemeTagRepository themeTagRepository;

    public List<MeetingCardResponse> getMyMeetingList(Long memberId) {
        List<GroupMember> myMemberships = groupMemberRepository.findByMemberId(memberId);
        if (myMemberships.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = myMemberships.stream()
                .map(GroupMember::getGroupId)
                .toList();

        Map<Long, com.bangawo.group.domain.Group> groupMap = groupRepository.findAllById(groupIds)
                .stream().collect(Collectors.toMap(com.bangawo.group.domain.Group::getId, g -> g));

        Map<Long, Meeting> latestMeetingMap = meetingRepository.findLatestByGroupIdIn(groupIds)
                .stream().collect(Collectors.toMap(Meeting::getGroupId, m -> m));

        Map<Long, List<GroupMember>> membersByGroup = groupMemberRepository.findByGroupIdIn(groupIds)
                .stream().collect(Collectors.groupingBy(GroupMember::getGroupId));

        Set<Long> allMemberIds = membersByGroup.values().stream()
                .flatMap(List::stream)
                .map(GroupMember::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberMap = memberRepository.findAllById(allMemberIds)
                .stream().collect(Collectors.toMap(Member::getId, m -> m));

        Set<String> themeCodes = groupMap.values().stream()
                .map(com.bangawo.group.domain.Group::getThemeTagCode)
                .collect(Collectors.toSet());

        Map<String, ThemeTag> themeTagMap = themeTagRepository.findByCodeIn(themeCodes)
                .stream().collect(Collectors.toMap(ThemeTag::getCode, t -> t));

        LocalDate today = LocalDate.now();

        List<MeetingCardResponse> cards = groupIds.stream()
                .filter(groupMap::containsKey)
                .filter(latestMeetingMap::containsKey)
                .map(groupId -> {
                    com.bangawo.group.domain.Group group = groupMap.get(groupId);
                    Meeting meeting = latestMeetingMap.get(groupId);
                    ThemeTag themeTag = themeTagMap.get(group.getThemeTagCode());
                    List<GroupMember> groupMembers = membersByGroup.getOrDefault(groupId, List.of());

                    List<MeetingCardResponse.MemberInfo> memberInfos = groupMembers.stream()
                            .sorted(Comparator.comparing(GroupMember::getJoinedAt))
                            .map(gm -> {
                                Member m = memberMap.get(gm.getMemberId());
                                return new MeetingCardResponse.MemberInfo(
                                        gm.getMemberId(),
                                        m != null ? m.getNickname() : null,
                                        m != null ? m.getProfileImageUrl() : null,
                                        gm.getAttendanceStatus()
                                );
                            })
                            .toList();

                    return new MeetingCardResponse(
                            groupId,
                            meeting.getId(),
                            group.getName(),
                            group.getThemeTagCode(),
                            themeTag != null ? themeTag.getDisplayName() : group.getThemeTagCode(),
                            meeting.computeListStatus(today),
                            meeting.getLocationStatus(),
                            meeting.getDateVoteStatus(),
                            null,
                            memberInfos.size(),
                            memberInfos
                    );
                })
                .sorted(Comparator
                        .comparingInt((MeetingCardResponse c) -> c.listStatus().ordinal())
                        .thenComparing(Comparator.comparing(
                                c -> latestMeetingMap.get(c.groupId()).getCreatedAt(),
                                Comparator.reverseOrder()
                        )))
                .toList();

        return cards;
    }
}
