package com.bangawo.meeting.infrastructure.persistence;

import com.bangawo.meeting.domain.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

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

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "category_labels", columnDefinition = "text[]")
    private List<String> categoryLabels;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "vibes", columnDefinition = "text[]")
    private List<String> vibes;

    @Column(name = "reservable")
    private Boolean reservable;

    @Column(name = "parking")
    private Boolean parking;

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
    private LocalDateTime confirmedDate;

    @Column(name = "pick_deadline")
    private LocalDateTime pickDeadline;

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
        entity.categoryLabels = meeting.getCategoryLabels();
        entity.vibes = meeting.getVibes();
        entity.reservable = meeting.getReservable();
        entity.parking = meeting.getParking();
        entity.status = meeting.getStatus();
        entity.locationStatus = meeting.getLocationStatus();
        entity.dateVoteStatus = meeting.getDateVoteStatus();
        entity.confirmedDate = meeting.getConfirmedDate();
        entity.pickDeadline = meeting.getPickDeadline();
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
                .categoryLabels(categoryLabels)
                .vibes(vibes)
                .reservable(reservable)
                .parking(parking)
                .status(status)
                .locationStatus(locationStatus)
                .dateVoteStatus(dateVoteStatus)
                .confirmedDate(confirmedDate)
                .pickDeadline(pickDeadline)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
