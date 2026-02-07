package ru.practicum.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    private String name;
}