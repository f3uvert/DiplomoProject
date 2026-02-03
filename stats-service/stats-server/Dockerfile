FROM openjdk:17-jdk-slim AS build
WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests -pl stats-service/stats-server -am

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/stats-service/stats-server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]