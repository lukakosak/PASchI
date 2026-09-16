FROM node:16-bullseye-slim AS build
WORKDIR /app

COPY Implementierung/frontend/paschi/package.json Implementierung/frontend/paschi/yarn.lock ./

RUN corepack enable && yarn install --frozen-lockfile

COPY Implementierung/frontend/paschi/ ./

ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN yarn vite build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80