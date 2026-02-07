package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Comment;
import ru.practicum.entity.CommentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatusAndParentCommentIsNull(Long eventId,
                                                               CommentStatus status,
                                                               Pageable pageable);

    List<Comment> findByParentCommentIdAndStatus(Long parentCommentId, CommentStatus status);

    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    List<Comment> findByEventIdAndStatusNot(Long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    boolean existsByEventIdAndAuthorId(Long eventId, Long authorId);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:users IS NULL OR c.author.id IN :users) " +
            "AND (:events IS NULL OR c.event.id IN :events) " +
            "AND (:statuses IS NULL OR c.status IN :statuses) " +
            "AND (:text IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Comment> findAdminComments(@Param("users") List<Long> users,
                                    @Param("events") List<Long> events,
                                    @Param("statuses") List<CommentStatus> statuses,
                                    @Param("text") String text,
                                    Pageable pageable);

    Long countByEventIdAndStatus(Long eventId, CommentStatus status);

    @Query("SELECT c.event.id, COUNT(c) FROM Comment c " +
            "WHERE c.event.id IN :eventIds AND c.status = 'PUBLISHED' " +
            "GROUP BY c.event.id")
    List<Object[]> countCommentsByEventIds(@Param("eventIds") List<Long> eventIds);

    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);
}