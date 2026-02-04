FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем весь stats-service проект
COPY ../../stats-service/pom.xml .
COPY ../../stats-service/stats-server/pom.xml ./stats-server/
COPY ../../stats-service/stats-dto/pom.xml ./stats-dto/
COPY ../../stats-service/stats-client/pom.xml ./stats-client/

# Скачиваем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код stats-server (основной сервис)
COPY ../../stats-service/stats-server/src ./stats-server/src

# Собираем stats-server с зависимостями
RUN mvn clean package -pl stats-server -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/stats-server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]