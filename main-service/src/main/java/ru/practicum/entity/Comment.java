package ru.practicum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000, columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

    @Column(name = "created_on", nullable = false)
    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "updated_on")
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Builder.Default
    private Long likesCount = 0L;

    @Builder.Default
    private Long dislikesCount = 0L;

    @Column(name = "is_reply")
    @Builder.Default
    private Boolean isReply = false;

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}