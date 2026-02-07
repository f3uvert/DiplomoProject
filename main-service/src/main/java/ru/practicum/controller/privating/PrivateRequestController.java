package ru.practicum.controller.privating;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests - получение запросов пользователя", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam(name = "eventId", required = true) @Positive Long eventId) {

        log.info("POST /users/{}/requests?eventId={} - создание запроса на участие", userId, eventId);
        return requestService.createParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {

        log.info("PATCH /users/{}/requests/{}/cancel - отмена запроса", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}