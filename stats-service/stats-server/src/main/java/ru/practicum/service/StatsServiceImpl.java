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
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        log.info("=== SAVE HIT ===");
        log.info("URI: {}, IP: {}, App: {}",
                endpointHitDto.getUri(), endpointHitDto.getIp(), endpointHitDto.getApp());

        EndpointHit hit = statsMapper.toEntity(endpointHitDto);
        EndpointHit saved = statsRepository.save(hit);
        log.info("Saved to database: ID={}", saved.getId());

        return statsMapper.toDto(saved);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("=== GET STATS ===");
        log.info("Params: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        validateDates(start, end);

        List<ViewStatsDto> result;

        try {
            if (Boolean.TRUE.equals(unique)) {
                result = statsRepository.getUniqueStats(start, end, uris);
            } else {
                result = statsRepository.getStats(start, end, uris);
            }

            if (result == null) {
                return Collections.emptyList();
            }

            log.info("Result from DB: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Error getting stats from database: {}", e.getMessage(), e);
            return Collections.emptyList();
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