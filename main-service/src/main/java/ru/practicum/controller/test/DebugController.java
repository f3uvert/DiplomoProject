package ru.practicum.controller.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/debug")
@Slf4j
@RequiredArgsConstructor
public class DebugController {

    private final StatsClient statsClient;

    @PostMapping("/send-hit")
    public String sendHit() {
        log.info("=== DEBUG: Testing StatsClient ===");

        try {
            // Вариант 1: Используем метод hit(String, String, String)
            log.info("Method 1: hit(String, String, String)");
            statsClient.hit("test-app", "/debug/test", "127.0.0.1");
            log.info("Method 1: Success");

            // Вариант 2: Используем метод hit(EndpointHitDto)
            log.info("Method 2: hit(EndpointHitDto)");
            EndpointHitDto dto = EndpointHitDto.builder()
                    .app("test-app-2")
                    .uri("/debug/test2")
                    .ip("192.168.1.101")
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.hit(dto);
            log.info("Method 2: Success");

            return "Hits sent successfully";

        } catch (Exception e) {
            log.error("StatsClient failed: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/check-stats")
    public String checkStats() {
        log.info("=== DEBUG: Checking stats ===");

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now(),
                    Arrays.asList("/debug/test", "/debug/test2"),
                    false
            );

            return "Stats: " + stats;

        } catch (Exception e) {
            log.error("Failed to get stats: {}", e.getMessage(), e);
            return "Error getting stats: " + e.getMessage();
        }
    }
}