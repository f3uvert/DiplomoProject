package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 10, max = 2000, message = "Comment must be between 10 and 2000 characters")
    private String text;

    private Long eventId;

    // Для ответов на комментарии
    private Long parentCommentId;
}