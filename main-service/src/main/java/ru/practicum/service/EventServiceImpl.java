package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.*;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ViewService viewService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours from now");
        }

        Event event = eventMapper.toEntity(eventDto, category, initiator);
        Event savedEvent = eventRepository.save(event);

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByInitiator(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByInitiator(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByInitiator(Long userId, Long eventId,
                                               UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != Event.EventState.PENDING &&
                event.getState() != Event.EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() != null &&
                updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.CANCEL_REVIEW) {
            event.setState(Event.EventState.CANCELED);
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<Event.EventState> states,
                                             List<Long> categories, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Event> events = eventRepository.findEventsWithOptionalFilters(users, states, categories, pageable);

        List<Event> filteredEvents = events.stream()
                .filter(event -> rangeStart == null || !event.getEventDate().isBefore(rangeStart))
                .filter(event -> rangeEnd == null || !event.getEventDate().isAfter(rangeEnd))
                .collect(Collectors.toList());

        return filteredEvents.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newEventDate = updateRequest.getEventDate();

            if (event.getPublishedOn() != null && newEventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from publication");
            }

            if (newEventDate.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Event date cannot be in the past");
            }
        }

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.PUBLISH_EVENT) {
                if (event.getState() != Event.EventState.PENDING) {
                    throw new ConflictException("Cannot publish event that is not in PENDING state");
                }
                event.setState(Event.EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.REJECT_EVENT) {
                if (event.getState() == Event.EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject already published event");
                }
                event.setState(Event.EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(updatedEvent);
    }

    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        log.info("=== GET PUBLIC EVENT {} ===", eventId);
        log.info("StatsClient is null? {}", statsClient == null); // ← Проверка

        Event event = eventRepository.findByIdAndState(eventId, Event.EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        log.info("Will send hit: app=ewm-main-service, uri={}, ip={}", uri, ip);

        // ОТПРАВКА HIT
        try {
            statsClient.hit("ewm-main-service", uri, ip);
            log.info("Hit sent via StatsClient");
        } catch (Exception e) {
            log.error("Failed to send hit: {}", e.getMessage(), e);
        }

        viewService.incrementAndGetViews(eventId, ip);

        Long realViews = getViewsFromStatsService(eventId);
        log.info("Real views from stats-service for event {}: {}", eventId, realViews);

        event.setViews(realViews);

        return eventMapper.toFullDto(event);
    }

    private Long getViewsFromStatsService(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now();
            String uri = "/events/" + eventId;

            List<ViewStatsDto> stats = statsClient.getStats(
                    start, end, Collections.singletonList(uri), true
            );

            if (!stats.isEmpty()) {
                Long views = stats.get(0).getHits();
                log.debug("Stats service returned {} views for event {}", views, eventId);
                return views;
            }
        } catch (Exception e) {
            log.warn("Could not get views from stats service: {}", e.getMessage());
        }

        return viewService.getViews(eventId);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {

        log.info("=== GET PUBLIC EVENTS ===");
        log.info("Params: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("RangeEnd cannot be before rangeStart");
        }

        try {
            log.info("Sending hit for /events search");
            statsClient.hit(
                    "ewm-main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr()
            );
        } catch (Exception e) {
            log.warn("Failed to send stats hit: {}", e.getMessage());
        }

        final LocalDateTime finalRangeStart = (rangeStart != null) ? rangeStart : LocalDateTime.now();
        final LocalDateTime finalRangeEnd = (rangeEnd != null) ? rangeEnd : LocalDateTime.now().plusYears(1);
        final Boolean finalOnlyAvailable = (onlyAvailable != null) ? onlyAvailable : false;
        final String finalText = (text != null) ? text.trim() : "";
        final List<Long> finalCategories = (categories != null) ? categories : Collections.emptyList();
        final Boolean finalPaid = paid;

        Pageable pageable = buildPageable(sort, from, size);

        List<Event> filteredEvents = getFilteredEvents(finalText, finalCategories, finalPaid, pageable);
        log.info("Found {} events after initial filtering", filteredEvents.size());

        List<Event> eventsAfterDateFilter = filteredEvents.stream()
                .filter(event -> !event.getEventDate().isBefore(finalRangeStart))
                .filter(event -> !event.getEventDate().isAfter(finalRangeEnd))
                .collect(Collectors.toList());
        log.info("{} events after date filter", eventsAfterDateFilter.size());

        List<Event> eventsAfterAvailabilityFilter = eventsAfterDateFilter.stream()
                .filter(event -> !finalOnlyAvailable ||
                        event.getParticipantLimit() == 0 ||
                        event.getConfirmedRequests() < event.getParticipantLimit())
                .collect(Collectors.toList());
        log.info("{} events after availability filter", eventsAfterAvailabilityFilter.size());

        List<Event> eventsAfterCategoryFilter = eventsAfterAvailabilityFilter;
        if (!finalCategories.isEmpty() && finalText.isEmpty()) {
            eventsAfterCategoryFilter = eventsAfterAvailabilityFilter.stream()
                    .filter(event -> finalCategories.contains(event.getCategory().getId()))
                    .collect(Collectors.toList());
            log.info("{} events after category filter", eventsAfterCategoryFilter.size());
        }

        List<Event> finalEventsList = eventsAfterCategoryFilter;
        if (finalPaid != null && finalText.isEmpty() && finalCategories.isEmpty()) {
            finalEventsList = eventsAfterCategoryFilter.stream()
                    .filter(event -> event.getPaid().equals(finalPaid))
                    .collect(Collectors.toList());
            log.info("{} events after paid filter", finalEventsList.size());
        }

        Map<Long, Long> viewsMap = getEventsViewsFromStatsService(
                finalEventsList.stream().map(Event::getId).collect(Collectors.toList())
        );
        log.info("Views map from stats-service: {}", viewsMap);

        return finalEventsList.stream()
                .map(event -> {
                    Long realViews = viewsMap.getOrDefault(event.getId(), 0L);

                    event.setViews(realViews);

                    return eventMapper.toShortDto(event);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getEventsViewsFromStatsService(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            log.info("No event IDs to get views for");
            return Collections.emptyMap();
        }

        log.info("Getting views from stats-service for {} events: {}", eventIds.size(), eventIds);

        Map<Long, Long> viewsMap = new HashMap<>();

        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now();

            List<String> uris = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            log.info("Requesting stats for URIs: {}", uris);

            List<ViewStatsDto> stats = statsClient.getStats(
                    start, end, uris, true
            );

            log.info("Received {} stats results from stats-service", stats.size());

            for (ViewStatsDto stat : stats) {
                try {
                    String uri = stat.getUri();
                    if (uri.startsWith("/events/")) {
                        Long eventId = Long.parseLong(uri.substring("/events/".length()));
                        viewsMap.put(eventId, stat.getHits());
                        log.debug("Event {} has {} views", eventId, stat.getHits());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse event id from uri: {}", stat.getUri());
                }
            }

        } catch (Exception e) {
            log.warn("Failed to get views from stats service: {}", e.getMessage());
            for (Long eventId : eventIds) {
                viewsMap.put(eventId, viewService.getViews(eventId));
            }
        }

        return viewsMap;
    }

    private List<Event> getFilteredEvents(String text, List<Long> categories, Boolean paid, Pageable pageable) {
        if (!text.isEmpty()) {
            log.info("Searching events by text: {}", text);
            return eventRepository.findPublishedEventsByText(text, pageable);
        } else if (!categories.isEmpty()) {
            log.info("Searching events by categories: {}", categories);
            return eventRepository.findPublishedEventsByCategories(categories, pageable);
        } else if (paid != null) {
            log.info("Searching events by paid: {}", paid);
            return eventRepository.findPublishedEventsByPaid(paid, pageable);
        } else {
            log.info("Getting all published events");
            return eventRepository.findPublishedEvents(pageable);
        }
    }

    private Long getEventViews(Long eventId) {
        log.info("Getting views for event {}...", eventId);


        Long localViews = viewService.getViews(eventId);
        log.info("Using local view service: {} views for event {}", localViews, eventId);

        return localViews;
    }

    private Map<Long, Long> getEventsViews(List<Long> eventIds) {
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStatsDto> stats = statsClient.getStats(
                start, end, uris, true
        );

        Map<Long, Long> viewsMap = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            try {
                String uri = stat.getUri();
                Long eventId = Long.parseLong(uri.substring("/events/".length()));
                viewsMap.put(eventId, stat.getHits());
            } catch (Exception e) {
                log.warn("Failed to parse event id from uri: {}", stat.getUri());
            }
        }

        return viewsMap;
    }

    private Pageable buildPageable(String sort, int from, int size) {
        log.info("Building pageable: sort={}, from={}, size={}", sort, from, size);

        if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            return PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        } else if ("VIEWS".equalsIgnoreCase(sort)) {
            return PageRequest.of(from / size, size);
        } else {
            return PageRequest.of(from / size, size);
        }
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().trim().isEmpty()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().trim().isEmpty()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null && !updateRequest.getEventDate().trim().isEmpty()) {
            LocalDateTime eventDate = LocalDateTime.parse(updateRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Event date must be at least 2 hours from now");
            }
            event.setEventDate(eventDate);
        }
        if (updateRequest.getLocation() != null) {
            Location location = Location.builder()
                    .lat(updateRequest.getLocation().getLat())
                    .lon(updateRequest.getLocation().getLon())
                    .build();
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().trim().isEmpty()) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.SEND_TO_REVIEW) {
            event.setState(Event.EventState.PENDING);
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().trim().isEmpty()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().trim().isEmpty()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = updateRequest.getEventDate();
            event.setEventDate(eventDate);
        }
        if (updateRequest.getLocation() != null) {
            Location location = Location.builder()
                    .lat(updateRequest.getLocation().getLat())
                    .lon(updateRequest.getLocation().getLon())
                    .build();
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().trim().isEmpty()) {
            event.setTitle(updateRequest.getTitle());
        }
    }
}