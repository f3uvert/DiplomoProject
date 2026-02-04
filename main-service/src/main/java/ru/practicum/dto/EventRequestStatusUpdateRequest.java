package ru.practicum.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "RequestIds cannot be empty")
    private List<Long> requestIds;

    @NotNull(message = "Status cannot be null")
    private Status status;

    public enum Status {
        CONFIRMED,  // Подтверждено
        REJECTED    // Отклонено
    }
}