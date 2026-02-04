FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем корневой pom
COPY ../pom.xml .

# Копируем модули
COPY pom.xml ./main-service/
COPY ../stats-service/stats-client/pom.xml ./stats-client/

# Скачиваем зависимости
RUN mvn dependency:go-offline

# Копируем исходный код main-service
COPY src ./main-service/src

# Собираем main-service
RUN mvn clean package -pl main-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]