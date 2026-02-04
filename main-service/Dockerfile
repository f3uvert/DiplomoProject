# Используем multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 1. Копируем корневой pom
COPY ../pom.xml .

# 2. Копируем модули
COPY pom.xml ./main-service/
COPY ../stats-service/stats-client/pom.xml ./stats-client/

# Скачиваем зависимости
RUN mvn dependency:go-offline

# 3. Копируем исходный код main-service
COPY src ./main-service/src

# 4. Собираем проект
RUN mvn clean package -pl main-service -am -DskipTests

# 5. Финальный образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем собранный jar
COPY --from=build /app/main-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]