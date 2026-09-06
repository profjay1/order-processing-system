# --- Build stage ---
# Angular 22 requires Node.js 22.12+
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:prod

# --- Serve stage ---
FROM nginx:1.27-alpine
COPY --from=build /app/dist/order-processing-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
