# Вариант 1: Amazon Corretto (гарантированно работает)
FROM amazoncorretto:17-alpine3.17

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]