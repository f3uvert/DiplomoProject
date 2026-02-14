package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.entity.CommentStatus;

import java.util.List;
import java.util.Map;

public interface CommentService {

    CommentDto getCommentById(Long commentId);

    List<CommentDto> getEventComments(Long eventId, int from, int size);

    List<CommentDto> getCommentReplies(Long commentId, int from, int size);

    CommentDto createComment(Long userId, NewCommentDto commentDto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateRequest);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    List<CommentDto> getAdminComments(List<Long> users, List<Long> events,
                                      List<CommentStatus> statuses, String text,
                                      int from, int size);

    CommentDto moderateComment(Long commentId, CommentStatus newStatus, String moderationReason);

    void deleteCommentByAdmin(Long commentId);

    Long getEventCommentsCount(Long eventId);

    Map<Long, Long> getCommentsCountForEvents(List<Long> eventIds);
}