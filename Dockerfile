FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Копируем весь проект
COPY . .

# Собираем только main-service и его зависимости
RUN mvn clean package -pl main-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar

# Создаем не-root пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]