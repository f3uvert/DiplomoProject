# Multi-stage build: собираем и запускаем в одном файле
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Копируем ВСЕ необходимое для сборки
COPY pom.xml .
COPY main-service/pom.xml ./main-service/
COPY main-service/src ./main-service/src/
COPY stats-service/stats-dto ./stats-service/stats-dto/
COPY stats-service/stats-client ./stats-service/stats-client/

# Собираем проект внутри Docker
RUN mvn clean package -DskipTests -pl main-service

# Финальный образ
FROM alpine:3.18

RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Копируем собранный JAR из этапа builder
COPY --from=builder /build/main-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]