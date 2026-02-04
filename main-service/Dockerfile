FROM nginx:alpine

RUN echo 'events{} http { server { listen 8080; location / { return 200 "OK"; } } }' > /etc/nginx/nginx.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]