package com.bangawo.meeting.application;

import com.bangawo.meeting.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteSchedulerService {

    private final DateVoteSessionRepository dateVoteSessionRepository;
    private final DateVoteOptionRepository dateVoteOptionRepository;
    private final DateVoteRecordRepository dateVoteRecordRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public void processExpiredSession(DateVoteSession session) {

        Meeting meeting = meetingRepository.findById(session.getMeetingId()).orElse(null);
        if (meeting == null) return;

        List<DateVoteOption> options = dateVoteOptionRepository.findBySessionId(session.getId());
        List<Long> optionIds = options.stream().map(DateVoteOption::getId).toList();
        List<DateVoteRecord> records = dateVoteRecordRepository.findByOptionIdIn(optionIds);

        if (records.isEmpty()) {
            session.expire();
            meeting.resetVote();
            dateVoteSessionRepository.save(session);
            meetingRepository.save(meeting);
            // TODO: FCM — 호스트에게 "투표자 없음, 직접 날짜 선택 요청" 알림 (FCM 유닛 완료 후 구현)
            return;
        }

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
            // TODO: FCM — 구성원 전체 "날짜 확정" 알림 (FCM 유닛 완료 후 구현)
        } else {
            session.expire();
            meeting.resetVote();
            // TODO: FCM — 호스트에게 "동률 발생, 직접 날짜 선택 요청" 알림 (FCM 유닛 완료 후 구현)
        }

        dateVoteSessionRepository.save(session);
        meetingRepository.save(meeting);
    }
}
