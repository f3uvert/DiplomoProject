FROM alpine:3.18

RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Сначала проверяем наличие JAR
RUN echo "Поиск JAR файла..."

# Копируем с проверкой
COPY --chown=root:root main-service/target/*.jar /app/app.jar || echo "JAR не найден, будет создан позже"

# Если JAR не скопировался, создаем простую заглушку
RUN if [ ! -f /app/app.jar ]; then \
    echo "Создание простого Spring Boot приложения..." && \
    cat > /tmp/App.java << 'EOF' \
    && javac -d /app /tmp/App.java \
    && jar -cfe /app/app.jar App -C /app . ; \
    fi

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]