package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ViewService {
    private final Map<String, Boolean> ipEventViews = new ConcurrentHashMap<>();

    public Long incrementAndGetViews(Long eventId, String ip) {
        String key = eventId + "_" + ip;

        log.info("Checking views for event {} from IP {}. Key: {}", eventId, ip, key);

        if (ipEventViews.containsKey(key)) {
            log.info("IP {} already viewed event {}. Not counting.", ip, eventId);
            // Если уже был, возвращаем текущее количество уникальных просмотров
            return getViews(eventId);
        }

        ipEventViews.put(key, true);
        Long totalViews = getViews(eventId);
        log.info("First view from IP {} for event {}. Total views now: {}", ip, eventId, totalViews);

        return totalViews;
    }

    public Long getViews(Long eventId) {
        long count = ipEventViews.keySet().stream()
                .filter(key -> key.startsWith(eventId + "_"))
                .count();
        log.debug("Event {} has {} unique views", eventId, count);
        return count;
    }

    public void clearViews() {
        ipEventViews.clear();
        log.info("Views cache cleared");
    }
}