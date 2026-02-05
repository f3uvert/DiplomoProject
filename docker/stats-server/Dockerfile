# Должен быть в корне stats-server/
FROM openjdk:17-jdk-slim

# Создаем директорию приложения
WORKDIR /app

# Копируем JAR файл (убедитесь в правильном имени)
COPY target/*.jar app.jar

# Открываем порт
EXPOSE 9090

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]