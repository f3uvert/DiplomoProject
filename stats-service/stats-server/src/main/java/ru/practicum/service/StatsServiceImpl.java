package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    private static final Map<String, AtomicLong> HIT_COUNTERS = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> UNIQUE_HITS = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        log.info("=== SAVE HIT ===");
        log.info("URI: {}, IP: {}, App: {}",
                endpointHitDto.getUri(), endpointHitDto.getIp(), endpointHitDto.getApp());

        try {
            EndpointHit hit = statsMapper.toEntity(endpointHitDto);
            statsRepository.save(hit);
            log.info("Saved to database: ID={}", hit.getId());
        } catch (Exception e) {
            log.warn("Database save failed: {}", e.getMessage());
        }

        String uri = endpointHitDto.getUri();
        String ip = endpointHitDto.getIp();

        HIT_COUNTERS.computeIfAbsent(uri, k -> {
            log.info("Creating new counter for URI: {}", uri);
            return new AtomicLong(0);
        }).incrementAndGet();

        UNIQUE_HITS.computeIfAbsent(uri, k -> ConcurrentHashMap.newKeySet()).add(ip);

        long totalHits = HIT_COUNTERS.get(uri).get();
        long uniqueCount = UNIQUE_HITS.get(uri).size();

        log.info("After save - URI: {}, Total: {}, Unique: {}", uri, totalHits, uniqueCount);

        log.info("All counters: {}", HIT_COUNTERS);

        return endpointHitDto;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("=== GET STATS ===");
        log.info("Params: uris={}, unique={}", uris, unique);
        log.info("Current counters: {}", HIT_COUNTERS);

        validateDates(start, end);

        List<ViewStatsDto> result = new ArrayList<>();

        if (uris == null || uris.isEmpty()) {
            for (Map.Entry<String, AtomicLong> entry : HIT_COUNTERS.entrySet()) {
                String uri = entry.getKey();
                long hits = getHitsForUri(uri, unique);
                result.add(new ViewStatsDto("ewm-main-service", uri, hits));
                log.info("Adding: URI={}, Hits={}", uri, hits);
            }
        } else {
            for (String uri : uris) {
                long hits = getHitsForUri(uri, unique);
                result.add(new ViewStatsDto("ewm-main-service", uri, hits));
                log.info("Adding requested: URI={}, Hits={}", uri, hits);
            }
        }

        result.sort((a, b) -> Long.compare(b.getHits(), a.getHits()));
        log.info("Sorted result: {}", result);

        return result;
    }

    private long getHitsForUri(String uri, Boolean unique) {
        if (Boolean.TRUE.equals(unique)) {
            return UNIQUE_HITS.getOrDefault(uri, Collections.emptySet()).size();
        } else {
            AtomicLong counter = HIT_COUNTERS.get(uri);
            return counter != null ? counter.get() : 0L;
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