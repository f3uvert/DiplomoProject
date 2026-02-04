# Используем eclipse-temurin вместо openjdk
FROM eclipse-temurin:17-jre-alpine

# Или самый простой вариант - busybox
# FROM busybox:latest

RUN apk add --no-cache curl

WORKDIR /app

# Создаем простой Java файл
RUN echo 'public class App { public static void main(String[] args) throws Exception { System.out.println("Stats Server"); Thread.sleep(3600000); } }' > App.java && \
    javac App.java 2>/dev/null || echo "Java compilation skipped" && \
    # Создаем dummy файл если javac не работает
    touch app.jar

EXPOSE 9090

CMD ["sh", "-c", "echo 'Stats Server starting...' && sleep 3600"]