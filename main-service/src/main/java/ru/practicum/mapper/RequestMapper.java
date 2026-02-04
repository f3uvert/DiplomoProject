package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.ParticipationRequest;
import ru.practicum.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RequestMapper {
    public ParticipationRequest toEntity(Event event, User requester) {
        return ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(ParticipationRequest.Status.PENDING)
                .build();
    }

    public ParticipationRequestDto toDto(ParticipationRequest entity) {
        return ParticipationRequestDto.builder()
                .id(entity.getId())
                .event(entity.getEvent().getId())
                .requester(entity.getRequester().getId())
                .created(entity.getCreated() != null ?
                        entity.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .status(entity.getStatus().name())
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}