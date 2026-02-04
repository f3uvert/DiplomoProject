# Используйте родительский образ для Java 17
FROM eclipse-temurin:17-jdk-alpine

# Установите Maven для сборки
RUN apk add --no-cache maven

WORKDIR /app

# Копируем исходный код
COPY pom.xml .
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# Копируем собранный JAR
RUN cp target/*.jar app.jar

# Удаляем исходный код чтобы уменьшить размер образа
RUN rm -rf src pom.xml target

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]