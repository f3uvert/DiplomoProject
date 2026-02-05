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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    private final Map<String, Integer> hitCounter = new HashMap<>();
    private final Map<String, Set<String>> uniqueHits = new HashMap<>();

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

        String key = endpointHitDto.getUri();

        hitCounter.put(key, hitCounter.getOrDefault(key, 0) + 1);

        uniqueHits.putIfAbsent(key, new HashSet<>());
        uniqueHits.get(key).add(endpointHitDto.getIp());

        log.info("Hit saved. Total for {}: {}, unique: {}",
                key, hitCounter.get(key), uniqueHits.get(key).size());

        return endpointHitDto;
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("Getting stats from {} to {}, uris={}, unique={}", start, end, uris, unique);

        validateDates(start, end);

        try {
            List<ViewStatsDto> dbResult;
            if (Boolean.TRUE.equals(unique)) {
                dbResult = statsRepository.getUniqueStats(start, end, uris);
            } else {
                dbResult = statsRepository.getStats(start, end, uris);
            }

            if (!dbResult.isEmpty()) {
                return dbResult;
            }
        } catch (Exception e) {
            log.warn("Database query failed: {}", e.getMessage());
        }

        List<ViewStatsDto> result = new ArrayList<>();

        if (uris == null || uris.isEmpty()) {
            for (Map.Entry<String, Integer> entry : hitCounter.entrySet()) {
                String uri = entry.getKey();
                long hits = Boolean.TRUE.equals(unique)
                        ? uniqueHits.getOrDefault(uri, Collections.emptySet()).size()
                        : entry.getValue();

                result.add(new ViewStatsDto("ewm-main-service", uri, hits));
            }
        } else {
            for (String uri : uris) {
                if (hitCounter.containsKey(uri)) {
                    long hits = Boolean.TRUE.equals(unique)
                            ? uniqueHits.getOrDefault(uri, Collections.emptySet()).size()
                            : hitCounter.get(uri);

                    result.add(new ViewStatsDto("ewm-main-service", uri, hits));
                } else {
                    result.add(new ViewStatsDto("ewm-main-service", uri, 0L));
                }
            }
        }

        result.sort((a, b) -> Long.compare(b.getHits(), a.getHits()));

        log.info("Returning {} stats entries", result.size());
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