# Вариант 1: Используйте eclipse-temurin (рекомендуется)
FROM eclipse-temurin:17-jdk-alpine

# Вариант 2: Или используйте slim версию с явным тегом
# FROM openjdk:17-slim

# Создаем рабочую директорию
WORKDIR /app

# Копируем JAR файл
COPY target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]