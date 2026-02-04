package ru.practicum.service;

import ru.practicum.dto.*;
import ru.practicum.entity.Event;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto eventDto);

    List<EventShortDto> getEventsByInitiator(Long userId, int from, int size);

    EventFullDto getEventByInitiator(Long userId, Long eventId);

    EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getAdminEvents(List<Long> users, List<Event.EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort,
                                        int from, int size, HttpServletRequest request);
}