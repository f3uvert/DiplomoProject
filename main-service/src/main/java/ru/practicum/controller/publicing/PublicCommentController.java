package ru.practicum.controller.publicing;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long commentId) {
        log.info("Public: получение комментария с id={}", commentId);
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/event/{eventId}")
    public List<CommentDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {

        log.info("Public: получение комментариев события id={}, from={}, size={}",
                eventId, from, size);
        return commentService.getEventComments(eventId, from, size);
    }

    @GetMapping("/{commentId}/replies")
    public List<CommentDto> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {

        log.info("Public: получение ответов на комментарий id={}, from={}, size={}",
                commentId, from, size);
        return commentService.getCommentReplies(commentId, from, size);
    }
}