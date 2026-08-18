# user-svc

Сервис пользователей для регистрации, авторизации, работы с профилями и ролями.

## Стек

- Java 25
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA
- Liquibase
- PostgreSQL
- JWT
- MapStruct
- Lombok

## Что делает сервис

- регистрирует пользователей
- аутентифицирует пользователей по `username` и `password`
- выдает `access` и `refresh` JWT токены
- хранит пользователей и роли в PostgreSQL
- поддерживает назначение ролей пользователям
- предоставляет ручки для работы с профилем пользователя

## Структура проекта

- `application` - бизнес-логика
- `domain` - JPA-сущности
- `infrastructure` - конфигурация, безопасность, исключения, репозитории
- `presentation` - REST-контроллеры и DTO

## Конфигурация

Приложение читает переменные окружения из файла `.env`:

```dotenv
POSTGRES_PASSWORD=user
POSTGRES_USERNAME=user
POSTGRES_URL=jdbc:postgresql://localhost:5432/user
JWT_SECRET=your-secret-key
EXPIRATION_ACCESS=90000
EXPIRATION_REFRESH=2592000000
```



## Модель авторизации

- `ROLE_USER` - роль по умолчанию для новых пользователей
- `ROLE_ADMIN` нужен для работы с ручками ролей
- только админ может:
  - создавать роли
  - удалять роли
  - назначать роли пользователям

## API

### Auth

`POST /api/v1/auth/register`

Регистрация нового пользователя.

Пример запроса:

```json
{
  "email": "user@example.com",
  "username": "user1",
  "password": "secret"
}
```

`POST /api/v1/auth/login`

Авторизация по `username` и `password`.

Пример запроса:

```json
{
  "username": "user1",
  "password": "secret"
}
```

`POST /api/v1/auth/refresh`

Обновление токенов.

Пример запроса:

```json
{
  "refreshToken": "eyJ..."
}
```

### Users

`GET /api/v1/users/{id}`

Получить пользователя по UUID.

`PUT /api/v1/users/{id}`

Обновить bio пользователя.

Пример запроса:

```json
{
  "bio": "New bio"
}
```

`DELETE /api/v1/users/{id}`

Удалить пользователя.

### Roles

Все ручки ролей доступны только пользователю с `ROLE_ADMIN`.

`POST /api/v1/roles`

Создать роль.

Пример запроса:

```json
{
  "name": "ADMIN"
}
```

`DELETE /api/v1/roles/{id}`

Удалить роль.

`POST /api/v1/roles/assign`

Назначить роль пользователю.

Пример запроса:

```json
{
  "userId": "7f2b2d1f-3c1e-4e3c-9c22-1a2b3c4d5e6f",
  "roleId": 2
}
```

## Запуск локально

1. Создай `.env` в корне проекта.
2. Подними PostgreSQL и создай базу.
3. Запусти приложение через Gradle:

```bash
./gradlew bootRun
```

## База данных

Liquibase создает:

- `users.users`
- `users.roles`
- `users.user_roles`

`UserEntity` связан с `RoleEntity` через many-to-many связь `user_roles`.

## Формат ошибок

Ошибки возвращаются в общем wrapper-формате:

```json
{
  "data": null,
  "error": {
    "code": "validation_error",
    "message": "email: must not be blank"
  },
  "httpStatusCode": 400
}
```

