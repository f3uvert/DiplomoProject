FROM alpine:3.18

RUN apk add --no-cache openjdk17-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]