package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.UpdateCommentAdminRequest;
import ru.practicum.entity.CommentStatus;
import ru.practicum.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {

        log.info("Admin: поиск комментариев: users={}, events={}, statuses={}, text={}, from={}, size={}",
                users, events, statuses, text, from, size);

        List<CommentStatus> statusList = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusList = statuses.stream()
                    .map(CommentStatus::valueOf)
                    .collect(Collectors.toList());
        }

        return commentService.getAdminComments(users, events, statusList, text, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(@PathVariable Long commentId,
                                      @Valid @RequestBody UpdateCommentAdminRequest updateRequest) {
        log.info("Admin: модерация комментария id={}, новый статус={}",
                commentId, updateRequest.getStatus());

        CommentStatus newStatus = CommentStatus.valueOf(updateRequest.getStatus());
        return commentService.moderateComment(commentId, newStatus, updateRequest.getModerationReason());
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Admin: удаление комментария id={}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}