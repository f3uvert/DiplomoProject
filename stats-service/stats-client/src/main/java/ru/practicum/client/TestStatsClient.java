package ru.practicum.client;

import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestStatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        StatsClient client = new StatsClient("http://localhost:9090");

        System.out.println("=== Тестирование сервиса статистики ===");

        LocalDateTime now = LocalDateTime.now();

        System.out.println("\n1. Отправляем хиты...");

        EndpointHitDto hit1 = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.100")
                .timestamp(now)
                .build();

        EndpointHitDto hit2 = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.101")
                .timestamp(now.plusMinutes(1))
                .build();

        EndpointHitDto hit3 = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.100") // тот же IP
                .timestamp(now.plusMinutes(2))
                .build();

        try {
            client.hit(hit1);
            System.out.println("Хит 1 отправлен");

            client.hit(hit2);
            System.out.println("Хит 2 отправлен");

            client.hit(hit3);
            System.out.println("Хит 3 отправлен");
        } catch (Exception e) {
            System.err.println("Ошибка отправки хитов: " + e.getMessage());
            return;
        }

        System.out.println("\n2. Получаем статистику...");

        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);

        try {

            System.out.println("\nТест 1: Все хиты");
            var allStats = client.getStats(start, end, null, false);
            printStats(allStats);


            System.out.println("\nТест 2: Только для /events/1");
            var uriStats = client.getStats(start, end,
                    java.util.List.of("/events/1"), false);
            printStats(uriStats);

            System.out.println("\nТест 3: Уникальные хиты (по IP)");
            var uniqueStats = client.getStats(start, end, null, true);
            printStats(uniqueStats);

            System.out.println("\nТест 4: Уникальные для /events/1");
            var uniqueUriStats = client.getStats(start, end,
                    java.util.List.of("/events/1"), true);
            printStats(uniqueUriStats);

        } catch (Exception e) {
            System.err.println("Ошибка получения статистики: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Тестирование завершено ===");
    }

    private static void printStats(java.util.List<ru.practicum.dto.ViewStatsDto> stats) {
        if (stats.isEmpty()) {
            System.out.println("   Нет данных");
            return;
        }

        for (var stat : stats) {
            System.out.println(String.format("   - %s | %s | хитов: %d",
                    stat.getApp(), stat.getUri(), stat.getHits()));
        }
    }
}