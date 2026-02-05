# Сборка stats-server
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем весь проект
COPY ../.. .

# Собираем stats-server
RUN mvn clean package -pl stats-service/stats-server -am -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/stats-service/stats-server/target/*.jar app.jar

RUN apk add --no-cache curl

EXPOSE 9090

# Запускаем
CMD ["java", "-jar", "app.jar", "--server.address=0.0.0.0"]