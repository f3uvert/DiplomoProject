package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.entity.Category;

@Component
public class CategoryMapper {
    public Category toEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toDto(Category entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}