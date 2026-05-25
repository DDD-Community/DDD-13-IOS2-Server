package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.DateVoteStatus;
import com.bangawo.meeting.domain.LocationStatus;
import com.bangawo.meeting.domain.Meeting;
import com.bangawo.meeting.domain.MeetingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meeting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MeetingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "theme_tag_code", nullable = false, length = 30)
    private String themeTagCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MeetingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_status", nullable = false, length = 15)
    private LocationStatus locationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_vote_status", nullable = false, length = 15)
    private DateVoteStatus dateVoteStatus;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static MeetingJpaEntity from(Meeting meeting) {
        MeetingJpaEntity entity = new MeetingJpaEntity();
        entity.id = meeting.getId();
        entity.groupId = meeting.getGroupId();
        entity.name = meeting.getName();
        entity.themeTagCode = meeting.getThemeTagCode();
        entity.status = meeting.getStatus();
        entity.locationStatus = meeting.getLocationStatus();
        entity.dateVoteStatus = meeting.getDateVoteStatus();
        entity.confirmedDate = meeting.getConfirmedDate();
        entity.createdAt = meeting.getCreatedAt();
        entity.updatedAt = meeting.getUpdatedAt();
        return entity;
    }

    public Meeting toDomain() {
        return Meeting.builder()
                .id(id)
                .groupId(groupId)
                .name(name)
                .themeTagCode(themeTagCode)
                .status(status)
                .locationStatus(locationStatus)
                .dateVoteStatus(dateVoteStatus)
                .confirmedDate(confirmedDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
