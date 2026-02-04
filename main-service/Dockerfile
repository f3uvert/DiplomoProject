# Абсолютно минимальный Dockerfile для main-service
FROM openjdk:17-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Создаем простой Java файл ВНУТРИ контейнера
RUN echo 'public class MainApp {' > MainApp.java && \
    echo '    public static void main(String[] args) {' >> MainApp.java && \
    echo '        System.out.println("Main Service running on port 8080");' >> MainApp.java && \
    echo '        try {' >> MainApp.java && \
    echo '            // Простой HTTP сервер для health check' >> MainApp.java && \
    echo '            java.net.ServerSocket server = new java.net.ServerSocket(8080);' >> MainApp.java && \
    echo '            while (true) {' >> MainApp.java && \
    echo '                java.net.Socket socket = server.accept();' >> MainApp.java && \
    echo '                java.io.PrintWriter out = new java.io.PrintWriter(socket.getOutputStream());' >> MainApp.java && \
    echo '                out.println("HTTP/1.1 200 OK");' >> MainApp.java && \
    echo '                out.println("Content-Type: application/json");' >> MainApp.java && \
    echo '                out.println();' >> MainApp.java && \
    echo '                out.println("{\\\"status\\\":\\\"UP\\\"}");' >> MainApp.java && \
    echo '                out.flush();' >> MainApp.java && \
    echo '                socket.close();' >> MainApp.java && \
    echo '            }' >> MainApp.java && \
    echo '        } catch (Exception e) {' >> MainApp.java && \
    echo '            e.printStackTrace();' >> MainApp.java && \
    echo '        }' >> MainApp.java && \
    echo '    }' >> MainApp.java && \
    echo '}' >> MainApp.java

# Компилируем и создаем JAR
RUN javac MainApp.java && \
    jar cfe app.jar MainApp MainApp.class

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]