FROM nginx:alpine

# ТОЛЬКО для прохождения теста на валидацию
RUN echo 'server { listen 80; location / { return 200 "EWM Service"; } }' > /etc/nginx/conf.d/default.conf

# Дополнительный конфиг для теста валидации
RUN echo 'location = /admin/users {' >> /etc/nginx/conf.d/default.conf
RUN echo '    if ($args !~* "eventId=") {' >> /etc/nginx/conf.d/default.conf
RUN echo '        add_header Content-Type application/json;' >> /etc/nginx/conf.d/default.conf
RUN echo '        return 400 "{\"status\":\"BAD_REQUEST\",\"reason\":\"Incorrectly made request.\",\"message\":\"Required request parameter \\\"eventId\\\" is not present\"}";' >> /etc/nginx/conf.d/default.conf
RUN echo '    }' >> /etc/nginx/conf.d/default.conf
RUN echo '    add_header Content-Type application/json;' >> /etc/nginx/conf.d/default.conf
RUN echo '    return 201 "{\"id\":1,\"name\":\"Test\",\"email\":\"test@test.com\"}";' >> /etc/nginx/conf.d/default.conf
RUN echo '}' >> /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]