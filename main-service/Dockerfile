FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

COPY pom.xml .
COPY main-service/pom.xml ./main-service/pom.xml
COPY stats-service/stats-client/pom.xml ./stats-service/stats-client/pom.xml
COPY stats-service/stats-dto/pom.xml ./stats-service/stats-dto/pom.xml

RUN mvn dependency:go-offline -B

COPY main-service ./main-service
COPY stats-service/stats-client ./stats-service/stats-client
COPY stats-service/stats-dto ./stats-service/stats-dto

RUN mvn clean package -DskipTests -pl main-service

FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=builder /app/main-service/target/*.jar app.jar

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]