# ПРОСТОЙ Dockerfile как у stats-service
FROM alpine:3.18

RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Копируйте СБОРНУЮ JAR из вашего реального проекта, не создавайте mock!
# Убедитесь что ваш main-service собран
COPY main-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]