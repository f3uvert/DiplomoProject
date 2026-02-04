# Упрощенный для stats-service
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Создаем минимальную структуру
RUN mkdir -p src/main/java/ru/practicum && \
    echo 'package ru.practicum; public class StatsApp { public static void main(String[] args) {} }' > src/main/java/ru/practicum/StatsApp.java && \
    echo '<?xml version="1.0"?><project><modelVersion>4.0.0</modelVersion><artifactId>stats-service</artifactId></project>' > pom.xml

# Компилируем dummy jar
RUN javac src/main/java/ru/practicum/StatsApp.java && \
    jar cf app.jar -C src/main/java ru/practicum/StatsApp.class

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 9090
CMD ["java", "-jar", "app.jar"]