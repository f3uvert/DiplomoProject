# docker-build/main-service/Dockerfile
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем весь проект (из корня, на уровень выше)
COPY ../.. .

# Собираем main-service и его зависимости
RUN mvn clean package -pl main-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar

# Устанавливаем curl для healthcheck и postgresql-client для проверки БД
RUN apk add --no-cache curl postgresql-client

EXPOSE 8080

# Скрипт ожидания БД и запуска
COPY wait-for-db.sh /wait-for-db.sh
RUN chmod +x /wait-for-db.sh

ENTRYPOINT ["/wait-for-db.sh", "ewm-db", "5432", "java", "-jar", "app.jar", "--server.address=0.0.0.0"]