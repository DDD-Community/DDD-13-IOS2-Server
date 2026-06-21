package com.bangawo.meeting.domain;

import com.bangawo.global.common.CategoryLabel;
import com.bangawo.global.error.BusinessException;
import com.bangawo.global.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class Meeting {

    private Long id;
    private Long groupId;
    private String name;
    private String themeTagCode;
    private List<String> categoryLabels;
    private List<String> vibes;
    private Boolean reservable;
    private Boolean parking;
    private MeetingStatus status;
    private LocationStatus locationStatus;
    private DateVoteStatus dateVoteStatus;
    private LocalDateTime confirmedDate;
    private LocalDateTime pickDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Meeting(Long id, Long groupId, String name, String themeTagCode,
                   List<String> categoryLabels, List<String> vibes, Boolean reservable, Boolean parking,
                   MeetingStatus status, LocationStatus locationStatus, DateVoteStatus dateVoteStatus,
                   LocalDateTime confirmedDate, LocalDateTime pickDeadline,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.themeTagCode = themeTagCode;
        this.categoryLabels = categoryLabels;
        this.vibes = vibes;
        this.reservable = reservable;
        this.parking = parking;
        this.status = status;
        this.locationStatus = locationStatus;
        this.dateVoteStatus = dateVoteStatus;
        this.confirmedDate = confirmedDate;
        this.pickDeadline = pickDeadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Meeting create(Long groupId, String name, String themeTagCode,
                                  List<String> categoryLabels, List<String> vibes,
                                  Boolean reservable, Boolean parking) {
        if (categoryLabels != null) {
            for (String label : categoryLabels) {
                if (!CategoryLabel.isValid(label)) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                }
            }
        }
        return Meeting.builder()
                .groupId(groupId)
                .name(name)
                .themeTagCode(themeTagCode)
                .categoryLabels(categoryLabels)
                .vibes(vibes)
                .reservable(reservable)
                .parking(parking)
                .status(MeetingStatus.ACTIVE)
                .locationStatus(LocationStatus.BEFORE)
                .dateVoteStatus(DateVoteStatus.BEFORE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void close() {
        this.status = MeetingStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isClosed() {
        return this.status == MeetingStatus.CLOSED;
    }

    public void startVote() {
        if (this.dateVoteStatus != DateVoteStatus.BEFORE) {
            throw new BusinessException(ErrorCode.VOTE_ALREADY_STARTED);
        }
        this.dateVoteStatus = DateVoteStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmDate(LocalDateTime dateTime) {
        this.confirmedDate = dateTime;
        this.dateVoteStatus = DateVoteStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void resetVote() {
        this.dateVoteStatus = DateVoteStatus.BEFORE;
        this.updatedAt = LocalDateTime.now();
    }

    public void assertCanStartLocationPhase() {
        if (this.dateVoteStatus != DateVoteStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.PLACE_PHASE_NOT_READY);
        }
        if (this.locationStatus != LocationStatus.BEFORE) {
            throw new BusinessException(ErrorCode.LOCATION_PHASE_ALREADY_STARTED);
        }
    }

    public void completeRecommendation() {
        this.locationStatus = LocationStatus.RECOMMENDED;
        this.pickDeadline = LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59).withNano(0);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPickDeadlineExpired() {
        return pickDeadline != null && LocalDateTime.now().isAfter(pickDeadline);
    }

    public void toVoting() {
        if (this.locationStatus != LocationStatus.RECOMMENDED) {
            throw new BusinessException(ErrorCode.LOCATION_NOT_RECOMMENDED);
        }
        this.locationStatus = LocationStatus.VOTING;
        this.updatedAt = LocalDateTime.now();
    }

    public void toConfirmed() {
        if (this.locationStatus != LocationStatus.VOTING) {
            throw new BusinessException(ErrorCode.PLACE_VOTE_NOT_IN_PROGRESS);
        }
        this.locationStatus = LocationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public MeetingListStatus computeListStatus(java.time.LocalDate today) {
        if (this.status == MeetingStatus.CLOSED) {
            return MeetingListStatus.CLOSED;
        }
        if (confirmedDate != null && confirmedDate.toLocalDate().isBefore(today)) {
            return MeetingListStatus.CLOSED;
        }
        if (locationStatus == LocationStatus.CONFIRMED && dateVoteStatus == DateVoteStatus.COMPLETED) {
            return MeetingListStatus.CONFIRMED;
        }
        return MeetingListStatus.IN_PROGRESS;
    }
}
