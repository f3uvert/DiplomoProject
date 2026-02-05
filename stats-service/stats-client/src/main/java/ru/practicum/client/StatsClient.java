package ru.practicum.client;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class StatsClient {
    private final String serverUrl;
    private final RestTemplate rest;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final boolean enabled;

    public StatsClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();

        String enabledEnv = System.getenv("STATS_CLIENT_ENABLED");
        this.enabled = !"false".equalsIgnoreCase(enabledEnv) &&
                serverUrl != null &&
                !serverUrl.isEmpty();

        log.info("StatsClient initialized. URL: {}, Enabled: {}", serverUrl, enabled);
    }

    public void hit(EndpointHitDto endpointHitDto) {
        if (!enabled) {
            log.debug("Stats client disabled, skipping hit: {}", endpointHitDto.getUri());
            return;
        }

        try {
            makeAndSendRequest(HttpMethod.POST, "/hit", null, endpointHitDto, null);
            log.debug("Hit sent successfully: {}", endpointHitDto.getUri());
        } catch (StatsClientException e) {
            log.warn("Failed to send hit to stats server: {}. Hit: {}",
                    e.getMessage(), endpointHitDto.getUri());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       @Nullable List<String> uris,
                                       @Nullable Boolean unique) {
        if (!enabled) {
            log.debug("Stats client disabled, returning empty stats");
            return Collections.emptyList();
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(FORMATTER));
        parameters.put("end", end.format(FORMATTER));

        StringBuilder path = new StringBuilder("/stats?start={start}&end={end}");

        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", String.join(",", uris));
            path.append("&uris={uris}");
        }

        if (unique != null) {
            parameters.put("unique", unique);
            path.append("&unique={unique}");
        }

        try {
            ResponseEntity<ViewStatsDto[]> response = makeAndSendRequest(
                    HttpMethod.GET,
                    path.toString(),
                    parameters,
                    null,
                    ViewStatsDto[].class
            );

            return response != null ? Arrays.asList(Objects.requireNonNull(response.getBody())) : Collections.emptyList();
        } catch (StatsClientException e) {
            log.warn("Failed to get stats from server: {}", e.getMessage());
            return Collections.emptyList(); // Возвращаем пустой список при ошибке
        }
    }

    public void hit(String app, String uri, String ip) {
        if (!enabled) {
            log.debug("Stats client disabled, skipping hit: {} {}", app, uri);
            return;
        }

        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            hit(hitDto);
        } catch (Exception e) {
            log.warn("Failed to send hit: {}", e.getMessage());
        }
    }

    private <T> ResponseEntity<T> makeAndSendRequest(HttpMethod method, String path,
                                                     @Nullable Map<String, Object> parameters,
                                                     @Nullable Object body,
                                                     Class<T> responseType) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders());

        try {
            if (parameters != null) {
                return rest.exchange(serverUrl + path, method, requestEntity, responseType, parameters);
            } else {
                return rest.exchange(serverUrl + path, method, requestEntity, responseType);
            }
        } catch (ResourceAccessException e) {
            throw new StatsClientException("Cannot connect to stats server at " + serverUrl + ": " + e.getMessage(), e);
        } catch (HttpStatusCodeException e) {
            throw new StatsClientException("Stats server returned error: " + e.getStatusCode() + " - " + e.getMessage(), e);
        }
    }

    private void makeAndSendRequest(HttpMethod method, String path,
                                    @Nullable Map<String, Object> parameters,
                                    @Nullable Object body) {
        makeAndSendRequest(method, path, parameters, body, Void.class);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public static class StatsClientException extends RuntimeException {
        public StatsClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}