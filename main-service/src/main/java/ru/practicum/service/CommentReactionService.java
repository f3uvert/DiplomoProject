package ru.practicum.service;

import ru.practicum.entity.ReactionType;

public interface CommentReactionService {

    void addReaction(Long userId, Long commentId, ReactionType reactionType);

    void removeReaction(Long userId, Long commentId);

    void changeReaction(Long userId, Long commentId, ReactionType newReactionType);

    Long getLikesCount(Long commentId);

    Long getDislikesCount(Long commentId);
}