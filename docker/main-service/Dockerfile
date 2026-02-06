FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# JAR находится в main-service/target/
COPY ../main-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]