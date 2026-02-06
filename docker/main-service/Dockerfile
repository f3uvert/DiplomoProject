FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY main-service/target/main-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]