# ---------- Fase 1: build ----------
# Node só para gerar os arquivos estáticos. Ele não vai para a imagem final.
FROM node:22-alpine AS build
WORKDIR /app

# package.json e o lock primeiro: a camada do npm ci fica em cache
# e só é refeita quando as dependências mudarem.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- Fase 2: execução ----------
# nginx sem root, como o backend. Essa imagem escuta na porta 8080.
FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 8080
