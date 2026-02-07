package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCountService {

    private final CommentService commentService;

    public Map<Long, Long> getCommentsCountForEvents(List<Long> eventIds) {
        Map<Long, Long> result = new HashMap<>();

        for (Long eventId : eventIds) {
            Long count = commentService.getEventCommentsCount(eventId);
            result.put(eventId, count);
        }

        return result;
    }
}