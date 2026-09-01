# Post Platform

Микросервисная платформа для публикации и просмотра постов с каналами, подписками, лайками, комментариями и уведомлениями.

Проект построен по принципу микросервисной архитектуры. Backend-сервисы написаны на Java + Spring Boot, API Gateway — на Go, frontend — на React + TypeScript.

---

## 🏗 Architecture

```text
                         ┌─────────────────┐
                         │    Frontend     │
                         │ React + TS/Vite │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   API Gateway   │
                         │       Go        │
                         │     :8080       │
                         └────────┬────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
       ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
       │  user-svc   │     │ content-svc │     │ social-svc  │
       │   :8090     │     │   :8082     │     │   :8083     │
       └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
              │                   │                   │
              ▼                   ▼                   ▼
        PostgreSQL          PostgreSQL             PostgreSQL

                                  │
                                  │ Kafka
                                  ▼
                         ┌─────────────────┐
                         │ notification-svc│
                         │      :8081      │
                         └────────┬────────┘
                                  │
                                  ▼
                            PostgreSQL

              ┌───────────────────┐
              │     feed-svc      │
              │      :8086        │
              └─────────┬─────────┘
                        │
                  gRPC │
                        ▼
                 content-svc
                 social-svc

                         ┌─────────────────┐
                         │     MinIO       │
                         │   S3 Storage    │
                         │    :9000/:9001  │
                         └─────────────────┘
```

---

## 📦 Services

### `api-gateway`

API Gateway написан на Go.

Отвечает за маршрутизацию HTTP-запросов между frontend и backend-сервисами.

Порт:

```text
8080
```

Основные маршруты:

```text
/api/v1/auth
/api/v1/channels
/api/v1/posts
/api/v1/media
/api/v1/subscriptions
/api/v1/likes
/api/v1/comments
/api/v1/feed
/api/v1/notifications
```

Gateway использует round-robin load balancing и передаёт JWT и необходимые HTTP-заголовки downstream-сервисам.

---

### `user-svc`

Сервис пользователей и аутентификации.

Порт:

```text
8090
```

Возможности:

* регистрация;
* авторизация;
* JWT access/refresh tokens;
* получение пользователя;
* изменение bio;
* удаление пользователя;
* создание ролей;
* назначение ролей пользователям.

Использует PostgreSQL и Liquibase.

---

### `content-svc`

Сервис контента.

Порт:

```text
8082
```

gRPC:

```text
9091
```

Отвечает за:

* создание каналов;
* редактирование каналов;
* удаление каналов;
* создание постов;
* редактирование постов;
* удаление постов;
* публикацию постов;
* модерацию;
* скрытие постов;
* загрузку изображений;
* получение изображений.

Для хранения файлов используется S3-compatible storage — MinIO.

Также сервис публикует события в Kafka.

---

### `social-svc`

Социальные взаимодействия пользователей.

Порт:

```text
8083
```

gRPC:

```text
9093
```

Возможности:

* подписка на каналы;
* отписка от каналов;
* получение подписок пользователя;
* лайки;
* удаление лайков;
* подсчёт лайков;
* комментарии;
* удаление комментариев.

Сервис взаимодействует с `content-svc` через gRPC.

Также использует Kafka для обработки событий.

---

### `feed-svc`

Сервис формирования ленты.

Порт:

```text
8086
```

Получает данные от:

```text
content-svc
social-svc
```

через gRPC.

Endpoint:

```text
GET /api/v1/feed
```

Поддерживает пагинацию:

```text
?page=0&size=20
```

---

### `notification-svc`

Сервис уведомлений.

Порт:

```text
8081
```

Получает события через Kafka и сохраняет уведомления в PostgreSQL.

Поддерживает:

* получение уведомлений;
* получение количества непрочитанных уведомлений;
* отметку уведомления как прочитанного.

---

### `frontend`

Frontend приложения написан на:

* React;
* TypeScript;
* Vite;
* Lucide React.

Frontend предоставляет интерфейс для:

* регистрации;
* авторизации;
* работы с каналами;
* создания постов;
* отправки постов на модерацию;
* публикации постов;
* загрузки изображений;
* подписок;
* лайков;
* комментариев;
* просмотра ленты;
* просмотра уведомлений.

При Docker-запуске frontend работает через Nginx.

---

# 🛠 Technologies

## Backend

* Java 25
* Spring Boot 4.1
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Kafka
* Spring gRPC
* Liquibase
* JWT
* MapStruct
* Lombok

## Gateway

* Go 1.25
* HTTP reverse proxy
* Round-robin load balancing
* Prometheus metrics

## Frontend

* React 19
* TypeScript
* Vite
* Lucide React
* Nginx

## Infrastructure

* Docker
* Docker Compose
* PostgreSQL 16
* Apache Kafka
* Zookeeper
* MinIO
* Prometheus
* Grafana

---

# 🚀 Quick Start

Для запуска проекта рекомендуется использовать Docker Compose.

### Требования

Перед запуском необходимо установить:

* Docker
* Docker Compose

Проверить установку:

```bash
docker --version
docker compose version
```

---

## 1. Клонирование проекта

```bash
git clone https://github.com/YOUR_USERNAME/post-platform.git
cd post-platform
```

---

## 2. Запуск проекта

Из корневой директории проекта выполните:

```bash
docker compose up --build
```

Docker соберёт и запустит:

* frontend;
* api-gateway;
* user-svc;
* content-svc;
* social-svc;
* feed-svc;
* notification-svc;
* PostgreSQL;
* Kafka;
* Zookeeper;
* MinIO;
* Prometheus;
* Grafana.

Первый запуск может занять некоторое время, поскольку Docker собирает Java и Go сервисы и устанавливает зависимости.

---

## 3. Открыть приложение

После успешного запуска frontend доступен по адресу:

```text
http://localhost:5173
```

API Gateway:

```text
http://localhost:8080
```

---

# 🔐 Authentication

Для работы с защищёнными endpoints используется JWT.

Регистрация:

```http
POST /api/v1/auth/register
```

Пример:

```json
{
  "email": "user@example.com",
  "username": "user",
  "password": "password"
}
```

Авторизация:

```http
POST /api/v1/auth/login
```

Пример:

```json
{
  "username": "user",
  "password": "password"
}
```

В ответ сервис возвращает:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

Frontend автоматически добавляет access token в:

```http
Authorization: Bearer <token>
```

При истечении access token frontend использует refresh token для получения новой пары токенов.

---

# 📝 Posts

Основные endpoints:

```text
POST   /api/v1/posts
GET    /api/v1/posts/{id}
GET    /api/v1/posts/channel/{channelId}
PUT    /api/v1/posts/{id}
DELETE /api/v1/posts/{id}
```

Работа со статусом поста:

```text
POST /api/v1/posts/{id}/moderation
POST /api/v1/posts/{id}/publish
POST /api/v1/posts/{id}/hide
```

Жизненный цикл поста:

```text
DRAFT
   │
   ▼
PENDING_MODERATION
   │
   ▼
PUBLISHED
```

Также предусмотрены статусы:

```text
HIDDEN
REJECTED
DELETED
```

---

# 📢 Channels

Работа с каналами:

```text
POST   /api/v1/channels
GET    /api/v1/channels/{id}
PUT    /api/v1/channels/{id}
DELETE /api/v1/channels/{id}
```

Канал содержит:

* ID;
* владельца;
* название;
* описание;
* дату создания.

---

# 👥 Subscriptions

Подписка на канал:

```text
POST /api/v1/subscriptions/channels/{id}/subscribe
```

Отписка:

```text
POST /api/v1/subscriptions/channels/{id}/unsubscribe
```

Получение подписок текущего пользователя:

```text
GET /api/v1/subscriptions/me
```

---

# ❤️ Likes

Поставить лайк:

```text
POST /api/v1/likes/{postId}/like
```

Убрать лайк:

```text
POST /api/v1/likes/{postId}/unlike
```

Количество лайков:

```text
GET /api/v1/likes/{postId}/count
```

---

# 💬 Comments

Создание комментария:

```text
POST /api/v1/comments/posts/{postId}
```

Получение комментариев:

```text
GET /api/v1/comments/posts/{postId}
```

Удаление комментария:

```text
DELETE /api/v1/comments/{commentId}
```

---

# 📰 Feed

Получение ленты:

```text
GET /api/v1/feed
```

Пагинация:

```text
GET /api/v1/feed?page=0&size=20
```

---

# 🔔 Notifications

Получение уведомлений:

```text
GET /api/v1/notifications?recipientId={userId}
```

Только непрочитанные:

```text
GET /api/v1/notifications?recipientId={userId}&unreadOnly=true
```

Количество непрочитанных:

```text
GET /api/v1/notifications/unread-count?recipientId={userId}
```

Отметить уведомление прочитанным:

```text
PATCH /api/v1/notifications/{id}/read
```

---

# 🖼 Media

Изображения постов хранятся в MinIO.

Загрузка изображения:

```text
POST /api/v1/media/{postId}/images
```

Request использует:

```text
multipart/form-data
```

Поле файла:

```text
file
```

Получение изображений поста:

```text
GET /api/v1/media/{postId}/images
```

---

# 📨 Kafka

Kafka используется для асинхронного обмена событиями между сервисами.

Основные события:

```text
content.post.published.v1
social.notification.requested.v1
```

Пример потока:

```text
content-svc
    │
    │ PostPublishedEvent
    ▼
  Kafka
    │
    ▼
social-svc
    │
    │ NotificationRequestedEvent
    ▼
  Kafka
    │
    ▼
notification-svc
```

Это позволяет сервисам оставаться слабо связанными и не вызывать друг друга синхронно для обработки событий.

---

# 🔗 gRPC

gRPC используется для синхронного взаимодействия между некоторыми сервисами.

```text
feed-svc
   │
   ├── gRPC ──> content-svc
   │
   └── gRPC ──> social-svc
```

Также `social-svc` использует gRPC для взаимодействия с `content-svc`.

---

# 🗄 Databases

Каждый основной сервис имеет собственную PostgreSQL базу.

```text
user-svc
    └── user database

content-svc
    └── content database

social-svc
    └── social database

notification-svc
    └── notification database
```

Порты PostgreSQL при локальном запуске:

| Database     | Port |
| ------------ | ---: |
| user         | 5432 |
| content      | 5430 |
| notification | 5433 |
| social       | 5439 |

Схема базы данных управляется с помощью Liquibase.

---

# 🪣 MinIO

MinIO используется как S3-compatible storage для изображений постов.

S3 API:

```text
http://localhost:9000
```

MinIO Console:

```text
http://localhost:9001
```

Для локального окружения используются:

```text
Username: minioadmin
Password: minioadmin
```

Bucket:

```text
post-platform-posts
```

---

# 📊 Monitoring

В проекте присутствуют Prometheus и Grafana.

Prometheus:

```text
http://localhost:9090
```

Grafana:

```text
http://localhost:3000
```

Grafana password:

```text
admin
```

---

# 🐳 Docker Services

После запуска:

```bash
docker compose ps
```

Основные контейнеры:

```text
post-platform-frontend
post-platform-api-gateway
post-platform-user-svc
post-platform-content-svc
post-platform-social-svc
post-platform-feed-svc
post-platform-notification-svc

user-db-pg
content-db-pg
social-db-pg
notification-db-pg

post-platform-kafka
post-platform-zookeeper
post-platform-minio
```

---

# 🛑 Stop

Остановить все контейнеры:

```bash
docker compose down
```

Остановить контейнеры и удалить volumes:

```bash
docker compose down -v
```

> `-v` удалит данные PostgreSQL и MinIO, поэтому использовать эту команду следует только если данные больше не нужны.

---

# 🔄 Rebuild

Если были изменения в коде:

```bash
docker compose up --build
```

Если нужно пересобрать проект полностью:

```bash
docker compose build --no-cache
docker compose up
```

---

# 💻 Local Development

Для разработки сервисы можно запускать отдельно.

### Java services

Проект использует Java 25.

Например:

```bash
cd user-svc
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

Аналогично запускаются:

```text
content-svc
social-svc
feed-svc
notification-svc
```

### API Gateway

Для запуска gateway:

```bash
cd api-gateway
go run ./cmd/gateway
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend запускается через Vite.

---

# 📁 Project Structure

```text
post-platform/
│
├── api-gateway/              # Go API Gateway
│
├── user-svc/                 # Authentication & users
│
├── content-svc/              # Channels, posts, media
│
├── social-svc/               # Likes, comments, subscriptions
│
├── feed-svc/                 # Feed generation
│
├── notification-svc/         # Notifications
│
├── frontend/                 # React frontend
│
├── docker/
│   ├── kafka/
│   ├── postgres/
│   │   ├── user/
│   │   ├── content/
│   │   ├── social/
│   │   └── notification/
│   ├── prometheus/
│   └── s3/
│
├── docker-compose.yaml
│
└── README.md
```

---

# 🔧 Configuration

Основные настройки находятся в:

```text
*/src/main/resources/application.yaml
```

Docker-specific настройки передаются через environment variables в `docker-compose.yaml`.

Например:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
APP_KAFKA_BOOTSTRAP_SERVERS
```

---

# ⚠️ Security

В текущем Docker Compose конфигурация содержит тестовые credentials и JWT secret для локальной разработки.

Перед использованием проекта в production необходимо:

* заменить JWT secret;
* изменить PostgreSQL credentials;
* изменить MinIO credentials;
* вынести secrets в environment variables/secrets manager;
* настроить HTTPS;
* ограничить доступ к инфраструктурным сервисам;
* не использовать development credentials в production.

---