# Используем multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build

# 1. Копируем все модули stats
WORKDIR /app

# Копируем родительский pom stats-service
COPY pom.xml .

# Копируем модули
COPY stats-server/pom.xml ./stats-server/
COPY stats-dto/pom.xml ./stats-dto/
COPY stats-client/pom.xml ./stats-client/

# Скачиваем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код stats-server (основной сервис)
COPY stats-server/src ./stats-server/src

# Собираем проект
RUN mvn clean package -pl stats-server -am -DskipTests

# 2. Финальный образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный jar из stats-server
COPY --from=build /app/stats-server/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]