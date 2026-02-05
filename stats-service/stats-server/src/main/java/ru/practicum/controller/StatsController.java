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
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") Boolean unique) {

        log.info("Raw params received - start: '{}', end: '{}'", start, end);

        try {
            start = URLDecoder.decode(start, StandardCharsets.UTF_8.toString());
            end = URLDecoder.decode(end, StandardCharsets.UTF_8.toString());

            log.info("Decoded params - start: '{}', end: '{}'", start, end);

            LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

            validateDates(startDate, endDate);

            return statsService.getStats(startDate, endDate, uris, unique);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss. Received: start='%s', end='%s'. Error: %s",
                            start, end, e.getMessage()),
                    e
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing request: " + e.getMessage(), e);
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