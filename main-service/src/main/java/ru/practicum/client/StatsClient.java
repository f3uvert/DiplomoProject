package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats.service.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.rest = builder.build();
        log.info("=== STATS CLIENT INITIALIZED ===");
        log.info("Server URL: {}", serverUrl);
    }

    public void hit(String app, String uri, String ip) {
        log.info("=== SENDING HIT ===");
        log.info("App: {}, URI: {}, IP: {}", app, uri, ip);

        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app(app)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Hit DTO: {}", hitDto);

            ResponseEntity<String> response = rest.postForEntity(
                    serverUrl + "/hit",
                    hitDto,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body: {}", response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("=== ERROR SENDING HIT ===");
            log.error("Stats service responded with status: {}", e.getStatusCode());
            log.error("Response body: {}", e.getResponseBodyAsString());
            log.error("Stats service URL was: {}", serverUrl);
        } catch (Exception e) {
            log.error("=== ERROR SENDING HIT ===");
            log.error("Error message: {}", e.getMessage());
            log.error("Stats service URL was: {}", serverUrl);
        }
    }

    public void hit(EndpointHitDto endpointHitDto) {
        log.info("=== SENDING HIT (DTO version) ===");
        log.info("DTO: {}", endpointHitDto);

        try {
            ResponseEntity<String> response = rest.postForEntity(
                    serverUrl + "/hit",
                    endpointHitDto,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());
            log.info("Response body: {}", response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("=== ERROR SENDING HIT (DTO) ===");
            log.error("Stats service responded with status: {}", e.getStatusCode());
            log.error("Response body: {}", e.getResponseBodyAsString());
            log.error("Stats service URL was: {}", serverUrl);
        } catch (Exception e) {
            log.error("=== ERROR SENDING HIT (DTO) ===");
            log.error("Error message: {}", e.getMessage());
            log.error("Stats service URL was: {}", serverUrl);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            String startStr = start.format(FORMATTER);
            String endStr = end.format(FORMATTER);

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(serverUrl + "/stats")
                    .queryParam("start", URLEncoder.encode(startStr, StandardCharsets.UTF_8))
                    .queryParam("end", URLEncoder.encode(endStr, StandardCharsets.UTF_8));

            if (uris != null && !uris.isEmpty()) {
                String urisParam = String.join(",", uris);
                builder.queryParam("uris", URLEncoder.encode(urisParam, StandardCharsets.UTF_8));
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.toUriString();
            log.info("Getting stats from: {}", url);

            ResponseEntity<ViewStatsDto[]> response = rest.getForEntity(url, ViewStatsDto[].class);

            ViewStatsDto[] body = response.getBody();
            if (body == null) {
                return Collections.emptyList();
            }

            return Arrays.asList(body);

        } catch (HttpStatusCodeException e) {
            log.error("Stats service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}