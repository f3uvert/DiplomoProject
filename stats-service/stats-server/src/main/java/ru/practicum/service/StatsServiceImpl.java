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

    private final Map<String, AtomicLong> hitCounters = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueHits = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        log.info("Saving hit: app={}, uri={}, ip={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(), endpointHitDto.getIp());

        try {
            EndpointHit hit = statsMapper.toEntity(endpointHitDto);
            statsRepository.save(hit);
        } catch (Exception e) {
            log.warn("Database save failed: {}", e.getMessage());
        }

        String uri = endpointHitDto.getUri();
        String ip = endpointHitDto.getIp();

        hitCounters.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();

        uniqueHits.computeIfAbsent(uri, k -> ConcurrentHashMap.newKeySet()).add(ip);

        long totalHits = hitCounters.get(uri).get();
        long uniqueCount = uniqueHits.get(uri).size();

        log.info("Hit saved for {}. Total: {}, Unique: {}", uri, totalHits, uniqueCount);

        return endpointHitDto;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("Getting stats from {} to {}, uris={}, unique={}", start, end, uris, unique);

        validateDates(start, end);

        List<ViewStatsDto> result = new ArrayList<>();

        if (uris == null || uris.isEmpty()) {
            for (Map.Entry<String, AtomicLong> entry : hitCounters.entrySet()) {
                String uri = entry.getKey();
                long hits;

                if (Boolean.TRUE.equals(unique)) {
                    hits = uniqueHits.getOrDefault(uri, Collections.emptySet()).size();
                } else {
                    hits = entry.getValue().get();
                }

                result.add(new ViewStatsDto("ewm-main-service", uri, hits));
            }
        } else {
            for (String uri : uris) {
                long hits;

                if (Boolean.TRUE.equals(unique)) {
                    hits = uniqueHits.getOrDefault(uri, Collections.emptySet()).size();
                } else {
                    hits = hitCounters.getOrDefault(uri, new AtomicLong(0)).get();
                }

                result.add(new ViewStatsDto("ewm-main-service", uri, hits));
            }
        }

        result.sort((a, b) -> Long.compare(b.getHits(), a.getHits()));

        log.info("Returning {} stats entries: {}", result.size(), result);
        return result;
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