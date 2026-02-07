package ru.practicum.controller.privating;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.service.CommentService;
import ru.practicum.service.CommentReactionService;
import ru.practicum.entity.ReactionType;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;
    private final CommentReactionService reactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @Valid @RequestBody NewCommentDto commentDto) {
        log.info("User {} создает комментарий к событию {}", userId, commentDto.getEventId());
        return commentService.createComment(userId, commentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody UpdateCommentRequest updateRequest) {
        log.info("User {} обновляет комментарий {}", userId, commentId);
        return commentService.updateComment(userId, commentId, updateRequest);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("User {} удаляет комментарий {}", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }

    @PostMapping("/{commentId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public void likeComment(@PathVariable Long userId,
                            @PathVariable Long commentId) {
        log.info("User {} ставит лайк комментарию {}", userId, commentId);
        reactionService.addReaction(userId, commentId, ReactionType.LIKE);
    }

    @PostMapping("/{commentId}/dislike")
    @ResponseStatus(HttpStatus.CREATED)
    public void dislikeComment(@PathVariable Long userId,
                               @PathVariable Long commentId) {
        log.info("User {} ставит дизлайк комментарию {}", userId, commentId);
        reactionService.addReaction(userId, commentId, ReactionType.DISLIKE);
    }

    @DeleteMapping("/{commentId}/reaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(@PathVariable Long userId,
                               @PathVariable Long commentId) {
        log.info("User {} удаляет реакцию на комментарий {}", userId, commentId);
        reactionService.removeReaction(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        log.info("Получение комментариев пользователя {}, from={}, size={}", userId, from, size);
        return commentService.getUserComments(userId, from, size);
    }
}