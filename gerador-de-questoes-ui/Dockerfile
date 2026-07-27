FROM node:22-alpine AS build-stage
WORKDIR /app

RUN npm install -g @angular/cli

COPY package*.json ./
RUN npm install

COPY . .

RUN sed -i 's/"outputMode": "server"/"outputMode": "static"/g' angular.json && \
    sed -i '/"server":/,/}/d' angular.json && \
    sed -i '/"ssr":/,/}/d' angular.json && \
    sed -i '/"prerender":/,/}/d' angular.json

RUN npx ng build --configuration production --output-path=/app/build_questoes

FROM nginx:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build-stage /app/build_questoes/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]