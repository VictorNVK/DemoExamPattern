# Демоэкзамен «Книжный магазин»

**КОД:** 09.02.07-2-2026  
**Специальность:** 09.02.07 «Информационные системы и программирование»  
**Предметная область:** интернет-магазин книг (авторизация, каталог товаров, заказы).

## Архитектура проекта

Проект разделён на **сервер** и **клиент**:

| Компонент | Каталог | Стек |
|-----------|---------|------|
| Backend (сервер, БД, API) | [`demo-exam-spring-backend/`](demo-exam-spring-backend/) | Java 21, Spring Boot 4, JPA, SQLite |
| Client (UI) | [`demo-exam-compose-postgres-template/`](demo-exam-compose-postgres-template/) | Kotlin, Compose Desktop, Ktor |

Клиент **не хранит** данные локально — все операции через REST API backend.

```
┌─────────────────────┐      HTTP/JSON       ┌──────────────────────────┐
│  Compose Desktop    │  ◄─────────────────► │  Spring Boot Backend     │
│  (Kotlin UI)        │   Bearer token       │  JPA + SQLite            │
└─────────────────────┘                      └──────────────────────────┘
                                                        │
                                                        ▼
                                              storage/database/
                                              book_store_exam.db
```

## Соответствие модулям ТЗ

| Модуль ТЗ | Документация | Что реализовано |
|-----------|--------------|-----------------|
| Модуль 1 — проектирование БД | [docs/MODULE_1.md](docs/MODULE_1.md) | SQLite, 3 таблицы, JPA-сущности |
| Модуль 2 — авторизация и список товаров | [docs/MODULE_2.md](docs/MODULE_2.md) | Login, роли, каталог, подсветка скидок |
| Модуль 3 — CRUD товаров | [docs/MODULE_3.md](docs/MODULE_3.md) | Поиск, фильтр, сортировка, редактор, фото |
| Модуль 4 — заказы | [docs/MODULE_4.md](docs/MODULE_4.md) | Список заказов для менеджера/админа |

## Быстрый старт

### Windows

**Терминал 1 — backend:**
```powershell
cd demo-exam-spring-backend
.\gradlew.bat bootRun
```

**Терминал 2 — клиент:**
```powershell
cd demo-exam-compose-postgres-template
.\gradlew.bat run
```

**Демо-данные в БД** (один раз, пока backend создал пустую схему):
```powershell
cd demo-exam-spring-backend
python scripts\seed_db.py
```

**Остановка** (задачи `gradlew stop` нет — используйте скрипты из **корня репозитория**):
```powershell
.\scripts\windows\stop-backend.ps1
.\scripts\windows\stop-client.ps1
.\scripts\windows\stop-all.ps1
```
Или `Ctrl+C` в терминале, где запущен `bootRun` / `run`.

### Linux

См. [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) и [scripts/linux/README.md](scripts/linux/README.md).

Остановка:
```bash
./scripts/linux/stop-backend.sh
./scripts/linux/stop-client.sh
./scripts/linux/stop-all.sh
```

## Учётные записи (после seed)

| Логин | Пароль | Роль | Возможности по ТЗ |
|-------|--------|------|-------------------|
| `client` | `client` | Клиент | Просмотр каталога |
| `manager` | `manager` | Менеджер | Каталог + поиск/фильтр/сортировка + заказы |
| `admin` | `admin` | Администратор | Всё выше + CRUD товаров и заказов |

## Полезные ссылки

| Ресурс | URL |
|--------|-----|
| Swagger UI | http://localhost:8082/api/v3/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8082/api/v3/api-docs |
| Полное развёртывание | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |
| Backend (детали API) | [demo-exam-spring-backend/README.md](demo-exam-spring-backend/README.md) |
| Client (конфиг API) | [demo-exam-compose-postgres-template/README.md](demo-exam-compose-postgres-template/README.md) |

## Структура репозитория

```
Демо экзамен файлы/
├── README.md                          ← этот файл
├── docs/
│   ├── MODULE_1.md … MODULE_4.md      ← документация по модулям ТЗ
│   └── DEPLOYMENT.md                  ← полное развёртывание
├── demo-exam-spring-backend/          ← Java backend
├── demo-exam-compose-postgres-template/ ← Kotlin client
└── scripts/linux/                     ← bash-скрипты для Linux
```

## Требования

| Компонент | Версия |
|-----------|--------|
| JDK (backend) | 21 |
| JDK (client) | 17+ (рекомендуется 21) |
| Python 3 | для `scripts/seed_db.py` (опционально) |

PostgreSQL, Docker и внешний сервер БД **не требуются**.
