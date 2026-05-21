package com.bangawo.meeting.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Meeting {

    private Long id;
    private Long groupId;
    private String name;
    private String themeTagCode;
    private LocationStatus locationStatus;
    private DateVoteStatus dateVoteStatus;
    private LocalDate confirmedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Meeting(Long id, Long groupId, String name, String themeTagCode,
                   LocationStatus locationStatus, DateVoteStatus dateVoteStatus,
                   LocalDate confirmedDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.themeTagCode = themeTagCode;
        this.locationStatus = locationStatus;
        this.dateVoteStatus = dateVoteStatus;
        this.confirmedDate = confirmedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Meeting createFirst(Long groupId, String name, String themeTagCode) {
        return Meeting.builder()
                .groupId(groupId)
                .name(name)
                .themeTagCode(themeTagCode)
                .locationStatus(LocationStatus.BEFORE)
                .dateVoteStatus(DateVoteStatus.BEFORE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public MeetingListStatus computeListStatus(LocalDate today) {
        if (confirmedDate != null && confirmedDate.isBefore(today)) {
            return MeetingListStatus.CLOSED;
        }
        if (locationStatus == LocationStatus.IN_PROGRESS || dateVoteStatus == DateVoteStatus.IN_PROGRESS) {
            return MeetingListStatus.IN_PROGRESS;
        }
        if (locationStatus == LocationStatus.COMPLETED && dateVoteStatus == DateVoteStatus.COMPLETED) {
            return MeetingListStatus.CONFIRMED;
        }
        return MeetingListStatus.IN_PROGRESS;
    }
}
