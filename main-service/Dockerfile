# Stage 1: Build
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /build

# Копируем ВЕСЬ проект (из корня)
COPY ../pom.xml ./pom.xml
COPY ../main-service ./main-service
COPY ../stats-client ./stats-client  # если есть

# Сначала собираем родительский pom
RUN mvn clean install -N -DskipTests

# Затем собираем main-service
WORKDIR /build/main-service
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /build/main-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]