package ru.practicum.controller.publicing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id,
                                 HttpServletRequest request) {
        return eventService.getPublicEvent(id, request);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            HttpServletRequest request) {

        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        return eventService.getPublicEvents(
                text, categories, paid, start, end, onlyAvailable, sort, from, size, request
        );
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}