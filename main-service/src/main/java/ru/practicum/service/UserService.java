package ru.practicum.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);

}