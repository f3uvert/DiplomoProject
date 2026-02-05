package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatsClient {
    private final String serverUrl;
    private final RestTemplate rest;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats.service.url:http://localhost:9090}") String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();
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
            e.printStackTrace(); // Добавьте это для отладки
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                uris.forEach(uri -> builder.queryParam("uris", uri));
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.toUriString();
            log.info("Getting stats from: {}", url);

            ResponseEntity<ViewStatsDto[]> response = rest.getForEntity(url, ViewStatsDto[].class);

            return Arrays.asList(response.getBody());

        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            e.printStackTrace(); // Добавьте это для отладки
            return Collections.emptyList();
        }
    }
}