package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT
    }
}