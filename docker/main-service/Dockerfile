# Сборка main-service
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем весь проект
COPY ../.. .

# Собираем main-service и его зависимости
RUN mvn clean package -pl main-service -am -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/main-service/target/*.jar app.jar

# Утилиты для проверки
RUN apk add --no-cache curl postgresql-client

# Ждем готовность базы данных
COPY wait-for-db.sh /wait-for-db.sh
RUN chmod +x /wait-for-db.sh

EXPOSE 8080

# Запускаем с ожиданием БД
CMD ["/wait-for-db.sh", "ewm-db", "5432", "java", "-jar", "app.jar", "--server.address=0.0.0.0"]