package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.entity.Compilation;
import ru.practicum.entity.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final EventMapper eventMapper;

    public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .events(events != null ? events : new HashSet<>())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .title(dto.getTitle())
                .build();
    }

    public CompilationDto toDto(Compilation entity) {
        return CompilationDto.builder()
                .id(entity.getId())
                .events(entity.getEvents().stream()
                        .map(eventMapper::toShortDto)
                        .collect(Collectors.toList()))
                .pinned(entity.getPinned())
                .title(entity.getTitle())
                .build();
    }

    public List<CompilationDto> toDtoList(List<Compilation> compilations) {
        return compilations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}