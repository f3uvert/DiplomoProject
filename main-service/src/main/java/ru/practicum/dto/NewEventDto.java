package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.parsing.Location;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Annotation cannot be blank")
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;

    @NotNull(message = "Category cannot be null")
    private Long category;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;

    @NotBlank(message = "Event date cannot be blank")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate; // String, не LocalDateTime!

    @NotNull(message = "Location cannot be null")
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @PositiveOrZero(message = "Participant limit must be positive or zero")
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
}