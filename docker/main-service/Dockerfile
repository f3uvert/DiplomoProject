FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY main-service/target/*.jar app.jar
EXPOSE 8080

RUN echo '#!/bin/sh' > /healthcheck.sh && \
    echo 'wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health 2>/dev/null || exit 1' >> /healthcheck.sh && \
    chmod +x /healthcheck.sh

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker-simple"]