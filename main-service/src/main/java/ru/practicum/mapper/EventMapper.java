package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.*;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public Event toEntity(NewEventDto dto, Category category, User initiator) {
        Location location = null;
        if (dto.getLocation() != null) {
            location = Location.builder()
                    .lat(dto.getLocation().getLat())
                    .lon(dto.getLocation().getLon())
                    .build();
        }

        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(LocalDateTime.parse(dto.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .initiator(initiator)
                .build();
    }

    public EventFullDto toFullDto(Event entity) {
        return toFullDto(entity, 0L); // По умолчанию 0 комментариев
    }

    public EventFullDto toFullDto(Event entity, Long commentsCount) {
        LocationDto locationDto = null;
        if (entity.getLocation() != null) {
            locationDto = LocationDto.builder()
                    .lat(entity.getLocation().getLat())
                    .lon(entity.getLocation().getLon())
                    .build();
        }

        return EventFullDto.builder()
                .id(entity.getId())
                .annotation(entity.getAnnotation())
                .category(categoryMapper.toDto(entity.getCategory()))
                .confirmedRequests(entity.getConfirmedRequests() != null ?
                        entity.getConfirmedRequests().longValue() : 0L)
                .createdOn(entity.getCreatedOn() != null ?
                        entity.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .description(entity.getDescription())
                .eventDate(entity.getEventDate() != null ?
                        entity.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .initiator(userMapper.toShortDto(entity.getInitiator()))
                .location(locationDto)
                .paid(entity.getPaid())
                .participantLimit(entity.getParticipantLimit())
                .publishedOn(entity.getPublishedOn() != null ?
                        entity.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .requestModeration(entity.getRequestModeration())
                .state(entity.getState() != null ? entity.getState().name() : null)
                .title(entity.getTitle())
                .views(entity.getViews() != null ? entity.getViews() : 0L)
                .commentsCount(commentsCount != null ? commentsCount : 0L)
                .build();
    }

    public EventShortDto toShortDto(Event entity) {
        return toShortDto(entity, 0L);
    }

    public EventShortDto toShortDto(Event entity, Long commentsCount) {
        return EventShortDto.builder()
                .id(entity.getId())
                .annotation(entity.getAnnotation())
                .category(categoryMapper.toDto(entity.getCategory()))
                .confirmedRequests(entity.getConfirmedRequests() != null ?
                        entity.getConfirmedRequests().longValue() : 0L)
                .eventDate(entity.getEventDate() != null ?
                        entity.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .initiator(userMapper.toShortDto(entity.getInitiator()))
                .paid(entity.getPaid())
                .title(entity.getTitle())
                .views(entity.getViews() != null ? entity.getViews() : 0L)
                .commentsCount(commentsCount != null ? commentsCount : 0L)
                .build();
    }
}