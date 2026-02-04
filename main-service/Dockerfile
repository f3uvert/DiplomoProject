FROM alpine:3.18

RUN apk add --no-cache openjdk17-jre

WORKDIR /app

COPY ./main-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]