package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.entity.Event;
import ru.practicum.entity.ParticipationRequest;
import ru.practicum.entity.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        // 1. Проверка пользователя
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Проверка события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // 3. Проверка: инициатор не может подать заявку на свое событие
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        // 4. Проверка: событие должно быть опубликовано
        if (event.getState() != Event.EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        // 5. Проверка: лимит участников
        if (event.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(
                    eventId, ParticipationRequest.Status.CONFIRMED
            );
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        // 6. Проверка: повторная заявка
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        // 7. Создание заявки
        ParticipationRequest request = requestMapper.toEntity(event, (ru.practicum.entity.User) requester);

        // 8. Если не требуется модерация или лимит = 0 - автоматическое подтверждение
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequest.Status.CONFIRMED);
            // Обновляем счетчик подтвержденных заявок в событии
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return requestMapper.toDto(savedRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository
                .findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (request.getStatus() == ParticipationRequest.Status.CONFIRMED) {
            // Уменьшаем счетчик подтвержденных заявок
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(ParticipationRequest.Status.CANCELED);
        ParticipationRequest canceledRequest = requestRepository.save(request);
        return requestMapper.toDto(canceledRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        // Проверяем, что событие принадлежит пользователю
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or not owned by user"));

        return requestRepository.findByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or not owned by user"));

        // Получаем заявки для обновления
        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        // Проверяем, что все заявки в состоянии PENDING
        requests.forEach(request -> {
            if (request.getStatus() != ParticipationRequest.Status.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        });

        // Проверяем лимит участников
        AtomicLong confirmedCount = new AtomicLong(requestRepository.countByEventIdAndStatus(
                eventId, ParticipationRequest.Status.CONFIRMED
        ));

        if (updateRequest.getStatus() == EventRequestStatusUpdateRequest.Status.CONFIRMED) {
            if (event.getParticipantLimit() > 0 &&
                    confirmedCount.get() + requests.size() > event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        // Обновляем статусы
        List<ParticipationRequestDto> confirmed = requests.stream()
                .filter(request -> {
                    if (updateRequest.getStatus() == EventRequestStatusUpdateRequest.Status.CONFIRMED) {
                        if (event.getParticipantLimit() == 0 ||
                                confirmedCount.get() < event.getParticipantLimit()) {
                            request.setStatus(ParticipationRequest.Status.CONFIRMED);
                            confirmedCount.getAndIncrement();
                            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                            return true;
                        } else {
                            request.setStatus(ParticipationRequest.Status.REJECTED);
                            return false;
                        }
                    } else {
                        request.setStatus(ParticipationRequest.Status.REJECTED);
                        return false;
                    }
                })
                .map(request -> {
                    requestRepository.save(request);
                    return requestMapper.toDto(request);
                })
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejected = requests.stream()
                .filter(request -> request.getStatus() == ParticipationRequest.Status.REJECTED)
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}