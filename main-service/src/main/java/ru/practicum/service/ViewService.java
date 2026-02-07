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

        if (ipEventViews.containsKey(key)) {
            return getViews(eventId);
        }

        ipEventViews.put(key, true);
        Long totalViews = getViews(eventId);

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