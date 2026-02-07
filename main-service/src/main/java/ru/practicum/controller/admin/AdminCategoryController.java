package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto categoryDto) {
        log.info("Admin: создание категории - '{}'", categoryDto.getName());
        log.debug("Получен запрос на создание категории: {}", categoryDto);

        CategoryDto createdCategory = categoryService.createCategory(categoryDto);

        log.info("Категория успешно создана: ID={}, Name='{}'",
                createdCategory.getId(), createdCategory.getName());
        return createdCategory;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Admin: удаление категории с ID={}", catId);

        categoryService.deleteCategory(catId);

        log.info("Категория с ID={} успешно удалена", catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Admin: обновление категории ID={}, новое имя: '{}'",
                catId, categoryDto.getName());
        log.debug("Данные для обновления: {}", categoryDto);

        CategoryDto updatedCategory = categoryService.updateCategory(catId, categoryDto);

        log.info("Категория успешно обновлена: ID={}, новое имя: '{}'",
                updatedCategory.getId(), updatedCategory.getName());
        return updatedCategory;
    }
}