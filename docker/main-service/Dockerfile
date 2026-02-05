FROM alpine:3.18

# Устанавливаем OpenJDK 17
RUN apk add --no-cache openjdk17-jre

WORKDIR /app
COPY target/*.jar app.jar

# Для healthcheck (опционально)
RUN apk add --no-cache curl

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]