package ru.practicum.controller.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/test-stats")
@RequiredArgsConstructor
public class TestStatsController {

    private final StatsClient statsClient;

    @PostMapping("/hit")
    public String testHit() {
        log.info("=== TEST: Sending hit via StatsClient ===");
        try {
            statsClient.hit("test-app", "/test-uri", "127.0.0.2");
            return "Hit sent successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/stats")
    public String testGetStats() {
        log.info("=== TEST: Getting stats via StatsClient ===");
        try {
            var stats = statsClient.getStats(
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now(),
                    Collections.singletonList("/test-uri"),
                    false
            );
            return "Stats received: " + stats;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}