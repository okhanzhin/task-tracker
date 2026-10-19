# Task Tracker

Учебный проект: планировщик задач на Spring Boot. Пользователь регистрируется, логинится, создаёт и редактирует свои задачи. Дополнительно есть фоновые сервисы — они отправляют приветственные письма и раз в день собирают сводку по задачам с помощью GigaChat.

## Модули

- **task-tracker-backend** — основной REST API (пользователи, задачи, JWT);
- **task-tracker-scheduler** — по расписанию собирает данные для ежедневного отчёта;
- **summarization-service** — делает текстовую сводку задач через GigaChat;
- **task-tracker-email-sender** — отправляет письма через SMTP;
- **task-tracker-common** — общие DTO для Kafka-сообщений.

## Технологии

- Java 21, Maven (multi-module)
- Spring Boot, Spring Security, Spring Data JPA
- PostgreSQL, Liquibase
- Apache Kafka
- JWT
- Docker, Docker Compose
- JUnit 5, Mockito, Testcontainers

## Что умеет

- регистрация и вход, токен выдаётся в заголовке `Authorization`;
- CRUD задач: создать, посмотреть список и одну задачу, обновить (PATCH), удалить;
- пользователь видит только свои задачи;
- после регистрации в Kafka уходит событие для приветственного письма;
- раз в сутки по cron планировщик забирает задачи пользователей, отправляет их на суммаризацию и рассылает готовые сводки на почту.

## API

Только те эндпоинты, которые есть в коде.

### Аутентификация

```text
POST /api/v1/auth/register  — регистрация
POST /api/v1/auth/login     — вход
POST /api/v1/auth/logout    — выход
```

JWT возвращается в заголовке ответа:

```text
Authorization: Bearer <token>
```

### Пользователь

```text
GET /api/v1/users
```

### Задачи

```text
POST   /api/v1/tasks
GET    /api/v1/tasks
GET    /api/v1/tasks/{taskId}
PATCH  /api/v1/tasks/{taskId}
DELETE /api/v1/tasks/{taskId}
```

Все операции с задачами требуют JWT.

### Внутренний API

```text
GET /internal/v1/reports/daily-data?from=...&to=...&limit=100&afterUserId=...
```

Используется только планировщиком. Период передаётся как `Instant`, страницы проходят по `afterUserId`. Нужен service-токен со scope `internal:reports:read`.

## Kafka-топики

| Топик | Что делает |
|---|---|
| `EMAIL_SENDING_TASKS` | задачи на отправку писем (welcome, ежедневный отчёт) |
| `SUMMARIZATION_REQUESTS` | запрос на сводку: задачи пользователя за период |
| `SUMMARIZATION_RESPONSES` | готовая сводка |

Автосоздание топиков выключено, топики объявлены в конфигурации сервисов.

## Запуск

Перед первым запуском скопируйте `.env.example` в `.env` и заполните значения:

```bash
cp .env.example .env
```

Затем:

```bash
docker compose up -d --build
```

Поднимутся PostgreSQL, Kafka и все сервисы. Backend будет доступен на `http://localhost:8080`.

Остановить:

```bash
docker compose down
```

## Локальная разработка

Инфраструктуру можно держать в Docker, а сервис запускать локально из IDE:

```bash
docker compose up -d postgresql kafka
```

В `application.yml` есть значения по умолчанию для localhost. Внутри Docker-сети сервисы обращаются друг к другу по именам контейнеров (`postgresql:5432`, `kafka:29092`). Секреты при локальном запуске передаются через переменные окружения.

## Тесты

```bash
./mvnw verify
```

Интеграционные тесты используют Testcontainers — нужен запущенный Docker. Есть интеграционные сценарии регистрации, аутентификации и работы с задачами, контрактные тесты Kafka-сообщений и тесты полного цикла отчёта. Тесты с суффиксом `ManualTest` ходят в реальный GigaChat API и запускаются вручную.

## CI

GitHub Actions: на push и pull request в `master` собирается проект и прогоняются тесты, потом образы сервисов публикуются в Docker Hub.
