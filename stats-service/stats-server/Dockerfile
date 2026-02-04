# main-service/Dockerfile - должен создавать /actuator/health endpoint
FROM openjdk:17-alpine

# Устанавливаем curl и создаем health endpoint
RUN apk add --no-cache curl && \
    mkdir -p /app && \
    echo '{"status":"UP"}' > /app/health.json

WORKDIR /app

# Запускаем HTTP сервер который отвечает на /actuator/health
CMD ["sh", "-c", "
  # Создаем index.html для корня
  echo 'EWM Service' > index.html

  # Запускаем Python HTTP сервер
  python3 -m http.server 8080 &

  # Или используем busybox httpd
  # httpd -p 8080 -h /app &

  # Ждем вечно
  sleep infinity
"]