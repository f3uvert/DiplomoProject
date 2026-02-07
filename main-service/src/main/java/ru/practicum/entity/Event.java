package ru.practicum.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "confirmed_requests")
    @Builder.Default
    private Integer confirmedRequests = 0;

    @Column(name = "created_on")
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "lon"))
    })
    private Location location;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "participant_limit")
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    @Builder.Default
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventState state = EventState.PENDING;

    @Column(nullable = false, length = 120)
    private String title;

    @Transient
    @Builder.Default
    private Long views = 0L;

    public enum EventState {
        PENDING, PUBLISHED, CANCELED
    }
}