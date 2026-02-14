package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.entity.Comment;
import ru.practicum.entity.CommentStatus;
import ru.practicum.entity.Event;
import ru.practicum.entity.User;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final UserMapper userMapper;

    public Comment toEntity(NewCommentDto dto, User author, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .author(author)
                .event(event)
                .status(CommentStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toDto(Comment entity) {
        CommentDto dto = CommentDto.builder()
                .id(entity.getId())
                .text(entity.getText())
                .author(userMapper.toShortDto(entity.getAuthor()))
                .eventId(entity.getEvent().getId())
                .status(entity.getStatus().name())
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .likesCount(entity.getLikesCount() != null ? entity.getLikesCount() : 0L)
                .dislikesCount(entity.getDislikesCount() != null ? entity.getDislikesCount() : 0L)
                .isReply(entity.getIsReply() != null ? entity.getIsReply() : false)
                .build();

        if (entity.getParentComment() != null) {
            dto.setParentCommentId(entity.getParentComment().getId());
        }

        return dto;
    }
}