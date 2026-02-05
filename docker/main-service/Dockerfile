# docker/main-service/Dockerfile
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем весь проект
COPY ../.. .

# Собираем main-service и его зависимости
RUN mvn clean package -pl main-service -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/main-service/target/*.jar app.jar

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

EXPOSE 8080

# ОЧЕНЬ ВАЖНО: Указываем слушать все интерфейсы
ENTRYPOINT ["java", "-jar", "app.jar", "--server.address=0.0.0.0"]