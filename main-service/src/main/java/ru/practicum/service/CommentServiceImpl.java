package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.entity.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Value("${comment.moderation.enabled:true}")
    private boolean moderationEnabled;

    @Value("${comment.reply-depth.max:1}")
    private int maxReplyDepth;

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Comment is not published");
        }

        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findByEventIdAndStatusAndParentCommentIsNull(
                eventId, CommentStatus.PUBLISHED, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentReplies(Long commentId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> replies = commentRepository.findByParentCommentIdAndStatus(
                commentId, CommentStatus.PUBLISHED);

        return replies.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findByAuthorId(userId, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = eventRepository.findById(commentDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != Event.EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        Comment comment = commentMapper.toEntity(commentDto, author, event);

        if (commentDto.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(commentDto.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));

            if (parentComment.getStatus() != CommentStatus.PUBLISHED) {
                throw new ConflictException("Cannot reply to unpublished comment");
            }

            if (parentComment.getIsReply()) {
                throw new ValidationException("Cannot reply to a reply (maximum depth reached)");
            }

            comment.setParentComment(parentComment);
            comment.setIsReply(true);
        }

        if (!moderationEnabled) {
            comment.setStatus(CommentStatus.PUBLISHED);
        }

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created: id={}, event={}, author={}",
                savedComment.getId(), event.getId(), author.getId());

        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateRequest) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found or not owned by user"));

        // Проверяем, что комментарий можно редактировать
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Cannot update deleted comment");
        }

        if (updateRequest.getText() != null && !updateRequest.getText().trim().isEmpty()) {
            comment.setText(updateRequest.getText());
            // При редактировании отправляем на повторную модерацию
            if (moderationEnabled) {
                comment.setStatus(CommentStatus.PENDING);
            }
        }

        comment.setUpdated(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found or not owned by user"));

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());
        commentRepository.save(comment);

        log.info("Comment deleted by author: id={}, author={}", commentId, userId);
    }

    @Override
    public List<CommentDto> getAdminComments(List<Long> users, List<Long> events,
                                             List<CommentStatus> statuses, String text,
                                             int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Comment> comments = commentRepository.findAdminComments(
                users, events, statuses, text, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto moderateComment(Long commentId, CommentStatus newStatus, String moderationReason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Cannot moderate deleted comment");
        }

        comment.setStatus(newStatus);
        comment.setUpdated(LocalDateTime.now());

        Comment moderatedComment = commentRepository.save(comment);

        log.info("Comment moderated: id={}, newStatus={}, reason={}",
                commentId, newStatus, moderationReason);

        return commentMapper.toDto(moderatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        commentRepository.delete(comment);

        log.info("Comment permanently deleted by admin: id={}", commentId);
    }

    @Override
    public Long getEventCommentsCount(Long eventId) {
        return commentRepository.countByEventIdAndStatus(eventId, CommentStatus.PUBLISHED);
    }

    @Override
    public Map<Long, Long> getCommentsCountForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> result = new HashMap<>();

        List<Object[]> counts = commentRepository.countCommentsByEventIds(eventIds);

        for (Object[] row : counts) {
            Long eventId = (Long) row[0];
            Long count = (Long) row[1];
            result.put(eventId, count);
        }

        for (Long eventId : eventIds) {
            result.putIfAbsent(eventId, 0L);
        }

        return result;
    }
}