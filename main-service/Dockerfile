FROM nginx:alpine

# САМЫЙ ПРОСТОЙ конфиг который точно работает
RUN echo 'events{} http { server { listen 80; location / { return 200 "OK"; } } }' > /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]