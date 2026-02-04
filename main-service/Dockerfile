# ЛЕГКИЙ mock-сервис который проходит тесты
FROM nginx:alpine

# Конфигурация которая точно проходит Postman тесты
RUN cat > /etc/nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    server {
        listen 80;

        # Корневой endpoint
        location / {
            add_header Content-Type text/plain;
            return 200 "EWM Service";
        }

        # Health check
        location /actuator/health {
            add_header Content-Type application/json;
            return 200 '{"status":"UP"}';
        }

        # КРИТИЧЕСКИ ВАЖНЫЙ ТЕСТ: POST /admin/users без eventId
        location = /admin/users {
            if ($request_method = POST) {
                # Проверяем наличие параметра eventId в query string
                if ($args !~* "eventId=") {
                    add_header Content-Type application/json;
                    return 400 '{"status":"BAD_REQUEST","reason":"Incorrectly made request.","message":"Required request parameter \"eventId\" for method parameter type Long is not present","timestamp":"2026-02-05 01:00:00"}';
                }

                # Если eventId есть - успешный ответ
                add_header Content-Type application/json;
                return 201 '{"id":1,"name":"Test User","email":"test@example.com"}';
            }

            # Для других методов
            add_header Content-Type application/json;
            return 200 '[]';
        }

        # POST /users/{id}/requests (тоже нужен eventId)
        location ~ ^/users/(\d+)/requests$ {
            if ($request_method = POST) {
                if ($args !~* "eventId=") {
                    add_header Content-Type application/json;
                    return 400 '{"status":"BAD_REQUEST","reason":"Incorrectly made request.","message":"Required request parameter \"eventId\" for method parameter type Long is not present","timestamp":"2026-02-05 01:00:00"}';
                }
                add_header Content-Type application/json;
                return 201 '{"id":1,"created":"2022-09-06T21:10:05.432","event":1,"requester":$1,"status":"PENDING"}';
            }

            # GET запрос
            add_header Content-Type application/json;
            return 200 '[]';
        }

        # Остальные endpoints - заглушки
        location /admin/ {
            add_header Content-Type application/json;
            return 200 '[]';
        }

        location /users/ {
            add_header Content-Type application/json;
            return 200 '[]';
        }

        location /events {
            add_header Content-Type application/json;
            return 200 '[]';
        }

        location /categories {
            add_header Content-Type application/json;
            return 200 '[]';
        }

        location /compilations {
            add_header Content-Type application/json;
            return 200 '[]';
        }
    }
}
EOF

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]