package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.CommentReactionRepository;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReactionServiceImpl implements CommentReactionService {

    private final CommentReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addReaction(Long userId, Long commentId, ReactionType reactionType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new ConflictException("Cannot react to unpublished comment");
        }

        if (reactionRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new ConflictException("User has already reacted to this comment");
        }

        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(user)
                .type(reactionType)
                .build();

        reactionRepository.save(reaction);

        updateCommentReactionCounts(comment);

        log.info("Reaction added: comment={}, user={}, type={}",
                commentId, userId, reactionType);
    }

    @Override
    @Transactional
    public void removeReaction(Long userId, Long commentId) {
        CommentReaction reaction = reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Reaction not found"));

        Comment comment = reaction.getComment();
        reactionRepository.delete(reaction);

        updateCommentReactionCounts(comment);

        log.info("Reaction removed: comment={}, user={}", commentId, userId);
    }

    @Override
    @Transactional
    public void changeReaction(Long userId, Long commentId, ReactionType newReactionType) {
        CommentReaction reaction = reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Reaction not found"));

        if (reaction.getType() == newReactionType) {
            throw new ConflictException("User already has this reaction type");
        }

        Comment comment = reaction.getComment();
        reaction.setType(newReactionType);
        reactionRepository.save(reaction);

        updateCommentReactionCounts(comment);

        log.info("Reaction changed: comment={}, user={}, newType={}",
                commentId, userId, newReactionType);
    }

    @Override
    public Long getLikesCount(Long commentId) {
        return reactionRepository.countByCommentIdAndType(commentId, ReactionType.LIKE);
    }

    @Override
    public Long getDislikesCount(Long commentId) {
        return reactionRepository.countByCommentIdAndType(commentId, ReactionType.DISLIKE);
    }

    private void updateCommentReactionCounts(Comment comment) {
        Long likes = getLikesCount(comment.getId());
        Long dislikes = getDislikesCount(comment.getId());

        comment.setLikesCount(likes);
        comment.setDislikesCount(dislikes);
        commentRepository.save(comment);
    }
}