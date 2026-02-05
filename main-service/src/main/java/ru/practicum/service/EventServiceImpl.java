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
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAdminEvents(
                users, states, categories, rangeStart, rangeEnd, pageable
        );

        return events.stream()
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
            if (newEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from publication");
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

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, Event.EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        statsClient.hit(
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        Long views = getEventViews(eventId);
        event.setViews(views);

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {

        log.info("Public events search: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        statsClient.hit(
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(1);
        }

        if (onlyAvailable == null) {
            onlyAvailable = false;
        }

        Pageable pageable = buildPageable(sort, from, size);

        List<Event> events = eventRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable
        );

        Map<Long, Long> viewsMap = getEventsViews(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        return events.stream()
                .map(event -> {
                    event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return eventMapper.toShortDto(event);
                })
                .collect(Collectors.toList());
    }


    private Long getEventViews(Long eventId) {
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + eventId;

        List<ViewStatsDto> stats = statsClient.getStats(
                start, end, List.of(uri), true
        );

        return stats.isEmpty() ? 0L : stats.get(0).getHits();
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
        if ("EVENT_DATE".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        } else if ("VIEWS".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("views").descending());
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