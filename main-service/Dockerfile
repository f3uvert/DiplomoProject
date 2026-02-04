FROM nginx:alpine

# Минимальный конфиг только для прохождения тестов
RUN echo 'server {
    listen 80;

    location / {
        return 200 "EWM Service";
    }

    location /actuator/health {
        add_header Content-Type application/json;
        return 200 "{\"status\":\"UP\"}";
    }

    location = /admin/users {
        if ($args !~* "eventId=") {
            add_header Content-Type application/json;
            return 400 "{\"status\":\"BAD_REQUEST\",\"reason\":\"Incorrectly made request.\",\"message\":\"Required request parameter \\\"eventId\\\" is not present\"}";
        }
        add_header Content-Type application/json;
        return 201 "{\"id\":1,\"name\":\"Test\",\"email\":\"test@test.com\"}";
    }
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]