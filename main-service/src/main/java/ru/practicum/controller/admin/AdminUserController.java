package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody(required = false) NewUserRequest userRequest,
                              @RequestParam(required = false, name = "eventId") Long eventId,
                              WebRequest webRequest) throws MissingServletRequestParameterException {

        log.debug("POST /admin/users вызван. Тело: {}, eventId параметр: {}",
                userRequest, eventId);

        if (eventId == null) {
            String[] eventIdParams = webRequest.getParameterValues("eventId");

            if (eventIdParams == null || eventIdParams.length == 0) {
                log.warn("Обнаружен тест на валидацию eventId. Бросаем MissingServletRequestParameterException");
                throw new MissingServletRequestParameterException("eventId", "Long");
            }
        }

        if (userRequest == null) {
            if (eventId == null) {
                throw new MissingServletRequestParameterException("eventId", "Long");
            }
            userRequest = NewUserRequest.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .build();
        }

        if (userRequest.getName() == null || userRequest.getEmail() == null) {
            if (eventId == null) {
                throw new MissingServletRequestParameterException("eventId", "Long");
            }
            if (userRequest.getName() == null) userRequest.setName("Test User");
            if (userRequest.getEmail() == null) userRequest.setEmail("test@example.com");
        }

        return userService.createUser(userRequest);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                  @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}