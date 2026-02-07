package ru.practicum.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.UserShortDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAdminDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private Long eventId;
    private String status;
    private String created;
    private String updated;
    private Long parentCommentId;
    private Long likesCount;
    private Long dislikesCount;
    private Boolean isReply;

    // Дополнительные поля
    private Boolean moderated;
    private String moderationReason;
}