# Этап 1: Сборка
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Создаем структуру проекта
RUN mkdir -p src/main/java/ru/practicum && \
    mkdir -p src/main/resources

# Создаем pom.xml
RUN echo '<?xml version="1.0" encoding="UTF-8"?> \
<project xmlns="http://maven.apache.org/POM/4.0.0" \
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 \
         http://maven.apache.org/xsd/maven-4.0.0.xsd"> \

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>ru.practicum</groupId>
    <artifactId>ewm-mock</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>' > pom.xml

# Создаем Spring Boot приложение
RUN echo 'package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class EwmMockApplication {

    private static Long userIdCounter = 1L;
    private static Long eventIdCounter = 1L;

    public static void main(String[] args) {
        SpringApplication.run(EwmMockApplication.class, args);
    }

    // Health check для wait-for-it.sh
    @GetMapping("/actuator/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    // Admin: Создание пользователя (POST /admin/users)
    @PostMapping("/admin/users")
    public Map<String, Object> createUser(@RequestBody Map<String, String> user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", userIdCounter++);
        response.put("name", user.get("name"));
        response.put("email", user.get("email"));
        return response;
    }

    // Admin: Получение пользователей (GET /admin/users)
    @GetMapping("/admin/users")
    public List<Map<String, Object>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        List<Map<String, Object>> users = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", i);
            user.put("name", "User " + i);
            user.put("email", "user" + i + "@example.com");
            users.add(user);
        }
        return users.stream().skip(from).limit(size).collect(java.util.stream.Collectors.toList());
    }

    // Public: Создание события (POST /users/{userId}/events)
    @PostMapping("/users/{userId}/events")
    public Map<String, Object> createEvent(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> eventDto) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", eventIdCounter++);
        response.put("title", eventDto.get("title"));
        response.put("annotation", eventDto.get("annotation"));
        response.put("description", eventDto.get("description"));
        response.put("eventDate", eventDto.get("eventDate"));
        response.put("paid", eventDto.getOrDefault("paid", false));
        response.put("participantLimit", eventDto.getOrDefault("participantLimit", 0));
        response.put("state", "PENDING");
        response.put("createdOn", java.time.LocalDateTime.now().toString());
        return response;
    }

    // Public: Получение событий (GET /events)
    @GetMapping("/events")
    public List<Map<String, Object>> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        List<Map<String, Object>> events = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", i);
            event.put("title", "Event " + i);
            event.put("annotation", "Annotation for event " + i);
            event.put("eventDate", java.time.LocalDateTime.now().plusDays(i).toString());
            event.put("paid", i % 2 == 0);
            event.put("confirmedRequests", 0);
            event.put("views", i * 10);
            events.add(event);
        }
        return events.stream().skip(from).limit(size).collect(java.util.stream.Collectors.toList());
    }
}' > src/main/java/ru/practicum/EwmMockApplication.java

# Создаем application.properties
RUN echo 'server.port=8080
server.address=0.0.0.0
spring.application.name=ewm-mock-service
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always' > src/main/resources/application.properties

# Собираем проект
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]