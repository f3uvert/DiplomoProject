
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

RUN mkdir -p src/main/java/ru/practicum && \
    mkdir -p src/main/resources

RUN cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
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
</project>
EOF

RUN cat > src/main/java/ru/practicum/MockApplication.java << 'EOF'
package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@SpringBootApplication
@RestController
public class MockApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockApplication.class, args);
    }

    @GetMapping("/actuator/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/")
    public String root() {
        return "EWM Service";
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> createUser(
            @RequestBody(required = false) Map<String, String> user,
            @RequestParam(name = "eventId", required = true) Long eventId) {



        if (user == null || user.isEmpty()) {
            // Если тело пустое - создаем фиктивного пользователя
            Map<String, Object> response = new HashMap<>();
            response.put("id", 1);
            response.put("name", "Test User");
            response.put("email", "test@example.com");
            return ResponseEntity.status(201).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", 1);
        response.put("name", user.get("name"));
        response.put("email", user.get("email"));
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<?> createRequest(
            @PathVariable Long userId,
            @RequestParam(name = "eventId", required = true) Long eventId) {

        Map<String, Object> response = new HashMap<>();
        response.put("id", 1);
        response.put("created", "2022-09-06T21:10:05.432");
        response.put("event", eventId);
        response.put("requester", userId);
        response.put("status", "PENDING");
        return ResponseEntity.status(201).body(response);
    }
}
EOF

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
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]