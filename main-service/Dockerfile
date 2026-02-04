# Вариант 1: Собираем весь проект из корня
FROM maven:3.9-eclipse-temurin-17 AS build

# Копируем весь проект из корня
WORKDIR /app

# Копируем корневой pom и все модули
COPY ../../pom.xml .
COPY ../../stats-service ./stats-service
COPY ../pom.xml ./main-service/
COPY ../src ./main-service/src

# Собираем только main-service
RUN mvn clean package -pl main-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]