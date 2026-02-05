package ru.practicum.client;



import jakarta.annotation.Nullable;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsClient {
    private final String serverUrl;
    private final RestTemplate rest;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();
    }

    public void hit(EndpointHitDto endpointHitDto) {
        makeAndSendRequest(HttpMethod.POST, "/hit", null, endpointHitDto, null);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       @Nullable List<String> uris,
                                       @Nullable Boolean unique) {
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

        ResponseEntity<ViewStatsDto[]> response = makeAndSendRequest(
                HttpMethod.GET,
                path.toString(),
                parameters,
                null,
                ViewStatsDto[].class
        );

        return response != null ? Arrays.asList(Objects.requireNonNull(response.getBody())) : Collections.emptyList();
    }

    public void hit(String app, String uri, String ip) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        hit(hitDto);
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
        } catch (HttpStatusCodeException e) {
            throw new StatsClientException("Ошибка при обращении к сервису статистики: " + e.getMessage(), e);
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