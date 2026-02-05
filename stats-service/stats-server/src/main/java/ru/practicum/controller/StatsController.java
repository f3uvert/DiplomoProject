package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsService;

import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Received POST /hit: {}", endpointHitDto);
        return statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") Boolean unique) {

        log.info("GET /stats called with start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        if (start == null || start.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'start' is required");
        }
        if (end == null || end.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'end' is required");
        }

        try {
            start = URLDecoder.decode(start, StandardCharsets.UTF_8);
            end = URLDecoder.decode(end, StandardCharsets.UTF_8);

            log.info("Decoded params - start: '{}', end: '{}'", start, end);

            LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

            validateDates(startDate, endDate);

            List<ViewStatsDto> result = statsService.getStats(startDate, endDate, uris, unique);
            log.info("Returning stats result: {}", result);

            return result;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss. Received: start='%s', end='%s'",
                            start, end),
                    e
            );
        } catch (Exception e) {
            log.error("Error processing /stats request", e);
            throw e;
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

}