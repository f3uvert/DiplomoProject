package ru.practicum.client;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class StatsClient {
    private final String serverUrl;
    private final RestTemplate rest;

    public StatsClient(@Value("${stats.service.url}") String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();
        log.info("=== STATS CLIENT INITIALIZED ===");
        log.info("Server URL: {}", serverUrl);
    }

    public void hit(String app, String uri, String ip) {
        log.info("=== SENDING HIT ===");
        log.info("App: {}, URI: {}, IP: {}", app, uri, ip);

        try {
            // Создаем DTO
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app(app)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Hit DTO: {}", hitDto);

            // Отправляем запрос
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(hitDto, headers);

            String url = serverUrl + "/hit";
            log.info("Sending POST to: {}", url);

            ResponseEntity<String> response = rest.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body: {}", response.getBody());

        } catch (Exception e) {
            log.error("=== ERROR SENDING HIT ===");
            log.error("Error message: {}", e.getMessage());
            log.error("Stats service URL was: {}", serverUrl);
            // НЕ бросаем исключение дальше - просто логируем
        }
    }

    public void hit(EndpointHitDto endpointHitDto) {
        log.info("=== SENDING HIT (DTO version) ===");
        log.info("DTO: {}", endpointHitDto);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);

            String url = serverUrl + "/hit";
            log.info("Sending POST to: {}", url);

            ResponseEntity<String> response = rest.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body: {}", response.getBody());

        } catch (Exception e) {
            log.error("=== ERROR SENDING HIT (DTO) ===");
            log.error("Error message: {}", e.getMessage());
            log.error("Stats service URL was: {}", serverUrl);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            // Форматируем даты
            String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Строим URL
            String url = String.format("%s/stats?start=%s&end=%s",
                    serverUrl,
                    URLEncoder.encode(startStr, StandardCharsets.UTF_8),
                    URLEncoder.encode(endStr, StandardCharsets.UTF_8));

            if (uris != null && !uris.isEmpty()) {
                url += "&uris=" + String.join(",", uris);
            }
            if (unique != null) {
                url += "&unique=" + unique;
            }

            log.info("Getting stats from: {}", url);

            ResponseEntity<ViewStatsDto[]> response = rest.getForEntity(
                    url,
                    ViewStatsDto[].class
            );

            return Arrays.asList(response.getBody());

        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}