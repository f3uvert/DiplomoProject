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

    private volatile boolean cacheInitialized = false;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        log.info("=== SAVE HIT - START ===");
        log.info("Received DTO: {}", endpointHitDto);

        EndpointHit hit = statsMapper.toEntity(endpointHitDto);
        log.info("Entity to save: {}", hit);

        EndpointHit savedHit = statsRepository.save(hit);
        log.info("Saved to DB with ID: {}", savedHit.getId());

        long count = statsRepository.count();
        log.info("Total hits in DB: {}", count);

        String uri = endpointHitDto.getUri();
        String ip = endpointHitDto.getIp();

        HIT_COUNTERS.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
        UNIQUE_HITS.computeIfAbsent(uri, k -> ConcurrentHashMap.newKeySet()).add(ip);

        log.info("=== SAVE HIT - END ===");
        log.info("URI: {}, Total hits: {}, Unique IPs: {}",
                uri,
                HIT_COUNTERS.get(uri).get(),
                UNIQUE_HITS.get(uri).size());

        return statsMapper.toDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("=== GET STATS - START ===");
        log.info("Input params:");
        log.info("  start: {} (type: {})", start, start.getClass().getSimpleName());
        log.info("  end: {} (type: {})", end, end.getClass().getSimpleName());
        log.info("  uris: {}", uris);
        log.info("  unique: {}", unique);

        validateDates(start, end);

        log.info("Initializing cache...");
        initializeCache();

        log.info("Cache status:");
        log.info("  HIT_COUNTERS size: {}", HIT_COUNTERS.size());
        HIT_COUNTERS.forEach((uri, counter) ->
                log.info("    URI '{}': {} hits", uri, counter.get()));

        log.info("  UNIQUE_HITS size: {}", UNIQUE_HITS.size());
        UNIQUE_HITS.forEach((uri, ips) ->
                log.info("    URI '{}': {} unique IPs", uri, ips.size()));

        log.info("Database check:");
        try {
            long totalHitsInDb = statsRepository.count();
            log.info("  Total hits in DB: {}", totalHitsInDb);

            if (totalHitsInDb > 0) {
                List<EndpointHit> allHits = statsRepository.findAll();
                log.info("  Last 5 hits in DB:");
                allHits.stream()
                        .skip(Math.max(0, allHits.size() - 5))
                        .forEach(hit -> log.info("    ID={}, app={}, uri={}, ip={}, timestamp={}",
                                hit.getId(), hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp()));
            }
        } catch (Exception e) {
            log.error("Error checking database: {}", e.getMessage());
        }

        log.info("Executing repository query...");
        List<ViewStatsDto> dbResult;

        try {
            if (Boolean.TRUE.equals(unique)) {
                log.info("Calling getUniqueStats()...");
                dbResult = statsRepository.getUniqueStats(start, end, uris);
                log.info("getUniqueStats returned {} results", dbResult != null ? dbResult.size() : "null");
            } else {
                log.info("Calling getStats()...");
                dbResult = statsRepository.getStats(start, end, uris);
                log.info("getStats returned {} results", dbResult != null ? dbResult.size() : "null");
            }

            if (dbResult != null && !dbResult.isEmpty()) {
                log.info("Repository query results:");
                dbResult.forEach(dto -> log.info("  {}: {} hits", dto.getUri(), dto.getHits()));
            } else {
                log.info("Repository query returned empty result or null");
            }
        } catch (Exception e) {
            log.error("Error in repository query: {}", e.getMessage(), e);
            dbResult = Collections.emptyList();
        }

        List<ViewStatsDto> result = new ArrayList<>();

        if (uris == null || uris.isEmpty()) {
            log.info("Processing all URIs from cache...");
            for (Map.Entry<String, AtomicLong> entry : HIT_COUNTERS.entrySet()) {
                String uri = entry.getKey();
                long hits = getHitsForUri(uri, unique);

                ViewStatsDto dto = ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri(uri)
                        .hits(hits)
                        .build();

                result.add(dto);
                log.info("Added to result: URI='{}', hits={}", uri, hits);
            }
        } else {
            log.info("Processing specific URIs: {}", uris);
            for (String uri : uris) {
                long hits = getHitsForUri(uri, unique);

                ViewStatsDto dto = ViewStatsDto.builder()
                        .app("ewm-main-service")
                        .uri(uri)
                        .hits(hits)
                        .build();

                result.add(dto);
                log.info("Added to result: URI='{}', hits={}", uri, hits);
            }
        }

        result.sort((a, b) -> Long.compare(b.getHits(), a.getHits()));

        log.info("Final result size: {}", result.size());
        log.info("Final result sorted:");
        result.forEach(dto -> log.info("  {}: {} hits", dto.getUri(), dto.getHits()));

        log.info("=== GET STATS - END ===");
        return result;
    }

    private synchronized void initializeCache() {
        if (!cacheInitialized) {
            log.info("Initializing cache from database...");
            List<EndpointHit> allHits = statsRepository.findAll();

            for (EndpointHit hit : allHits) {
                String uri = hit.getUri();
                String ip = hit.getIp();

                HIT_COUNTERS.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
                UNIQUE_HITS.computeIfAbsent(uri, k -> ConcurrentHashMap.newKeySet()).add(ip);
            }

            cacheInitialized = true;
            log.info("Cache initialized. Total URIs: {}", HIT_COUNTERS.size());
        }
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