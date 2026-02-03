package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.entity.EndpointHit;

@Component
public class StatsMapper {

    public EndpointHit toEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }

        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto toDto(EndpointHit entity) {
        if (entity == null) {
            return null;
        }

        return EndpointHitDto.builder()
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }
}