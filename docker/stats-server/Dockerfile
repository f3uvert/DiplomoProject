FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY stats-service/stats-server/target/*.jar app.jar
EXPOSE 9090

# Создаем простой health check скрипт
RUN echo '#!/bin/sh' > /healthcheck.sh && \
    echo 'wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health 2>/dev/null || exit 1' >> /healthcheck.sh && \
    chmod +x /healthcheck.sh

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker-simple"]