# Linux-скрипты для demo-exam

Стек: **Spring Boot backend** + **SQLite** + **Compose Desktop client**.

Все команды выполняйте из корня репозитория (`Демо экзамен файлы/`):

```bash
chmod +x scripts/linux/*.sh
```

## Быстрый старт

```bash
# 1. Системные пакеты (Ubuntu/Debian, один раз)
./scripts/linux/install-deps.sh

# 2. Gradle-зависимости + компиляция
./scripts/linux/setup-project.sh

# 3. SQLite: схема + демо-данные
./scripts/linux/init-database.sh

# 4. Backend (фон) + клиент
./scripts/linux/run-all.sh
```

Или одной командой после `install-deps.sh`:

```bash
./scripts/linux/setup-all.sh
./scripts/linux/run-all.sh
```

## Скрипты

| Скрипт | Назначение |
|--------|------------|
| `install-deps.sh` | apt: JDK 21, Python 3, curl, библиотеки Compose |
| `setup-project.sh` | `chmod +x gradlew`, загрузка зависимостей, компиляция |
| `init-database.sh` | Создание SQLite-схемы + seed |
| `seed-database.sh` | Только перезагрузка данных в существующую БД |
| `run-backend.sh` | Backend на переднем плане |
| `run-backend-background.sh` | Backend в фоне |
| `stop-backend.sh` | Остановка backend |
| `stop-client.sh` | Остановка клиента |
| `stop-all.sh` | Остановка backend и клиента |
| `run-client.sh` | Compose-клиент |
| `run-all.sh` | Backend (фон) + клиент |
| `build-all.sh` | Сборка обоих проектов |
| `setup-all.sh` | setup-project + init-database |

## Переменные окружения

| Переменная | По умолчанию | Описание |
|------------|--------------|----------|
| `RESET_DB=1` | — | Удалить БД перед `init-database.sh` |
| `BACKEND_PORT` | `8082` | Порт backend (читается из `application.properties`) |
| `WAIT_BACKEND=0` | `1` | Не ждать backend в `run-client.sh` |

Пример пересоздания БД:

```bash
RESET_DB=1 ./scripts/linux/init-database.sh
```

## Файлы

- SQLite: `demo-exam-spring-backend/storage/database/book_store_exam.db`
- Лог backend: `demo-exam-spring-backend/storage/backend.log`
- Swagger: http://localhost:8082/api/v3/swagger-ui/index.html

## Остановка

```bash
./scripts/linux/stop-backend.sh
./scripts/linux/stop-client.sh
./scripts/linux/stop-all.sh
```

## Учётные записи (после seed)

| Логин | Пароль | Роль |
|-------|--------|------|
| client | client | клиент |
| manager | manager | менеджер |
| admin | admin | администратор |

## Примечания

- PostgreSQL и Docker **не нужны** — используется SQLite в backend.
- Клиент обращается к API по `api.base_url` в `demo-exam-compose-postgres-template/src/main/resources/application.properties`.
- Gradle Wrapper уже в проектах — отдельно Gradle ставить не нужно.
