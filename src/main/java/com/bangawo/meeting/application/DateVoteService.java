package com.bangawo.meeting.application;

import com.bangawo.auth.domain.Member;
import com.bangawo.auth.domain.MemberRepository;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import com.bangawo.group.domain.GroupMember;
import com.bangawo.group.domain.GroupMemberRepository;
import com.bangawo.group.domain.GroupMemberRole;
import com.bangawo.meeting.domain.*;
import com.bangawo.meeting.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateVoteService {

    private static final Set<Integer> VALID_DURATION_DAYS = Set.of(1, 3, 7);

    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DateVoteSessionRepository dateVoteSessionRepository;
    private final DateVoteOptionRepository dateVoteOptionRepository;
    private final DateVoteRecordRepository dateVoteRecordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void startHostPick(Long meetingId, Long memberId, LocalDateTime date) {
        Meeting meeting = getMeeting(meetingId);
        requireHost(meeting.getGroupId(), memberId);

        if (date == null || !date.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_CANDIDATE_DATE);
        }

        meeting.startVote();
        meeting.confirmDate(date);
        meetingRepository.save(meeting);

        DateVoteSession session = DateVoteSession.ofHostPick(meetingId);
        dateVoteSessionRepository.save(session);
    }

    @Transactional
    public void startVote(Long meetingId, Long memberId, StartVoteRequest request) {
        Meeting meeting = getMeeting(meetingId);
        requireHost(meeting.getGroupId(), memberId);

        List<LocalDateTime> candidateDates = request.candidateDates();
        if (candidateDates == null || candidateDates.isEmpty() || candidateDates.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_CANDIDATE_COUNT);
        }

        LocalDateTime now = LocalDateTime.now();
        for (LocalDateTime d : candidateDates) {
            if (!d.isAfter(now)) {
                throw new BusinessException(ErrorCode.INVALID_CANDIDATE_DATE);
            }
        }

        long distinctCount = candidateDates.stream().distinct().count();
        if (distinctCount != candidateDates.size()) {
            throw new BusinessException(ErrorCode.INVALID_CANDIDATE_DATE);
        }

        if (!VALID_DURATION_DAYS.contains(request.durationDays())) {
            throw new BusinessException(ErrorCode.INVALID_DURATION_DAYS);
        }

        meeting.startVote();
        meetingRepository.save(meeting);

        DateVoteSession session = DateVoteSession.ofVote(meetingId, request.durationDays());
        DateVoteSession savedSession = dateVoteSessionRepository.save(session);

        List<DateVoteOption> options = new ArrayList<>();
        for (int i = 0; i < candidateDates.size(); i++) {
            options.add(DateVoteOption.builder()
                    .sessionId(savedSession.getId())
                    .candidateDate(candidateDates.get(i))
                    .sortOrder(i)
                    .build());
        }
        dateVoteOptionRepository.saveAll(options);
    }

    @Transactional
    public void submitVote(Long meetingId, Long memberId, SubmitVoteRequest request) {
        Meeting meeting = getMeeting(meetingId);

        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        if (meeting.getDateVoteStatus() != DateVoteStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.VOTE_NOT_IN_PROGRESS);
        }

        DateVoteSession session = dateVoteSessionRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_IN_PROGRESS));

        if (!session.getDeadline().isAfter(LocalDate.now().minusDays(1))) {
            throw new BusinessException(ErrorCode.VOTE_CLOSED);
        }

        List<DateVoteOption> sessionOptions = dateVoteOptionRepository.findBySessionId(session.getId());
        Set<Long> validOptionIds = sessionOptions.stream()
                .map(DateVoteOption::getId)
                .collect(Collectors.toSet());

        for (Long optionId : request.optionIds()) {
            if (!validOptionIds.contains(optionId)) {
                throw new BusinessException(ErrorCode.VOTE_OPTION_NOT_FOUND);
            }
        }

        dateVoteRecordRepository.deleteByOptionIdInAndMemberId(List.copyOf(validOptionIds), memberId);

        List<DateVoteRecord> records = request.optionIds().stream()
                .map(optionId -> DateVoteRecord.of(optionId, memberId))
                .toList();
        dateVoteRecordRepository.saveAll(records);

        checkEarlyCompletion(meeting, session, sessionOptions);
    }

    @Transactional(readOnly = true)
    public VoteStatusResponse getVoteStatus(Long meetingId, Long memberId) {
        Meeting meeting = getMeeting(meetingId);

        groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroupId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        DateVoteSession session = dateVoteSessionRepository.findByMeetingId(meetingId)
                .orElse(null);

        if (session == null) {
            return new VoteStatusResponse(
                    meeting.getDateVoteStatus(), null, null, List.of()
            );
        }

        List<DateVoteOption> options = dateVoteOptionRepository.findBySessionId(session.getId());
        List<Long> optionIds = options.stream().map(DateVoteOption::getId).toList();
        List<DateVoteRecord> records = dateVoteRecordRepository.findByOptionIdIn(optionIds);

        Map<Long, List<DateVoteRecord>> recordsByOption = records.stream()
                .collect(Collectors.groupingBy(DateVoteRecord::getOptionId));

        Set<Long> voterMemberIds = records.stream()
                .map(DateVoteRecord::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberMap = memberRepository.findAllById(voterMemberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        List<VoteStatusResponse.VoteOptionInfo> optionInfos = options.stream()
                .map(opt -> {
                    List<DateVoteRecord> optRecords = recordsByOption.getOrDefault(opt.getId(), List.of());
                    boolean isMyVote = optRecords.stream().anyMatch(r -> r.getMemberId().equals(memberId));

                    List<VoteStatusResponse.VoterInfo> voters = optRecords.stream()
                            .map(r -> {
                                Member m = memberMap.get(r.getMemberId());
                                boolean active = m != null && m.isActive();
                                return new VoteStatusResponse.VoterInfo(
                                        r.getMemberId(),
                                        active ? m.getNickname() : null,
                                        active ? m.getProfileImageUrl() : null
                                );
                            })
                            .toList();

                    return new VoteStatusResponse.VoteOptionInfo(
                            opt.getId(),
                            opt.getCandidateDate(),
                            optRecords.size(),
                            isMyVote,
                            voters
                    );
                })
                .sorted((a, b) -> {
                    if (b.voteCount() != a.voteCount()) return b.voteCount() - a.voteCount();
                    return options.stream().filter(o -> o.getId().equals(a.optionId())).findFirst()
                            .map(DateVoteOption::getSortOrder).orElse(0)
                            - options.stream().filter(o -> o.getId().equals(b.optionId())).findFirst()
                            .map(DateVoteOption::getSortOrder).orElse(0);
                })
                .toList();

        return new VoteStatusResponse(
                meeting.getDateVoteStatus(),
                session.getStatus(),
                session.getDeadline(),
                optionInfos
        );
    }

    @Transactional
    public void confirmDate(Long meetingId, Long memberId, ConfirmDateRequest request) {
        Meeting meeting = getMeeting(meetingId);
        requireHost(meeting.getGroupId(), memberId);

        if (meeting.getDateVoteStatus() != DateVoteStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.VOTE_NOT_IN_PROGRESS);
        }

        DateVoteSession session = dateVoteSessionRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_IN_PROGRESS));

        DateVoteOption option = dateVoteOptionRepository.findById(request.optionId())
                .filter(o -> o.getSessionId().equals(session.getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_OPTION_NOT_FOUND));

        meeting.confirmDate(option.getCandidateDate());
        session.confirm();

        meetingRepository.save(meeting);
        dateVoteSessionRepository.save(session);
    }

    private void checkEarlyCompletion(Meeting meeting, DateVoteSession session, List<DateVoteOption> options) {
        int totalMembers = groupMemberRepository.countByGroupId(meeting.getGroupId());
        List<Long> optionIds = options.stream().map(DateVoteOption::getId).toList();
        long votedMembers = dateVoteRecordRepository.countDistinctMemberIdByOptionIdIn(optionIds);

        if (totalMembers != votedMembers) {
            return;
        }

        List<DateVoteRecord> records = dateVoteRecordRepository.findByOptionIdIn(optionIds);
        Map<Long, Long> countByOption = records.stream()
                .collect(Collectors.groupingBy(DateVoteRecord::getOptionId, Collectors.counting()));

        long maxCount = countByOption.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        List<Long> topOptions = countByOption.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();

        if (topOptions.size() == 1) {
            DateVoteOption winner = options.stream()
                    .filter(o -> o.getId().equals(topOptions.get(0)))
                    .findFirst()
                    .orElseThrow();
            meeting.confirmDate(winner.getCandidateDate());
            session.confirm();
        } else {
            meeting.resetVote();
            session.expire();
            // TODO: FCM — 호스트에게 "동률, 직접 날짜 선택 요청" 알림 (FCM 유닛 완료 후 구현)
        }

        meetingRepository.save(meeting);
        dateVoteSessionRepository.save(session);
    }

    private Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
    }

    private void requireHost(Long groupId, Long memberId) {
        GroupMember caller = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        if (caller.getRole() != GroupMemberRole.HOST) {
            throw new BusinessException(ErrorCode.NOT_GROUP_HOST);
        }
    }
}
