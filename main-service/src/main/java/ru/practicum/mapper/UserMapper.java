package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.entity.User;

@Component
public class UserMapper {
    public User toEntity(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public UserDto toDto(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .build();
    }

    public UserShortDto toShortDto(User entity) {
        return UserShortDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}