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
        log.info("Saving hit to database: {}", endpointHitDto);

        EndpointHit hit = statsMapper.toEntity(endpointHitDto);
        EndpointHit savedHit = statsRepository.save(hit);

        List<EndpointHit> allHits = statsRepository.findAllHits();
        log.info("Total hits in DB after save: {}", allHits.size());
        allHits.forEach(h -> log.info("  DB Hit: id={}, app={}, uri={}, ip={}, timestamp={}",
                h.getId(), h.getApp(), h.getUri(), h.getIp(), h.getTimestamp()));

        return statsMapper.toDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        log.info("Getting stats from {} to {}, uris={}, unique={}", start, end, uris, unique);

        validateDates(start, end);

        List<EndpointHit> allHits = statsRepository.findAllHits();
        log.info("Total hits in DB before query: {}", allHits.size());
        allHits.forEach(h -> {
            boolean inRange = !h.getTimestamp().isBefore(start) && !h.getTimestamp().isAfter(end);
            log.info("  Hit: uri={}, timestamp={}, inRange={}", h.getUri(), h.getTimestamp(), inRange);
        });

        List<ViewStatsDto> result;
        if (Boolean.TRUE.equals(unique)) {
            log.info("Calling getUniqueStats");
            result = statsRepository.getUniqueStats(start, end, uris);
        } else {
            log.info("Calling getStats");
            result = statsRepository.getStats(start, end, uris);
        }

        log.info("Query returned {} results", result.size());
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