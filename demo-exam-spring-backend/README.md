# Demo Exam Spring Backend

REST API для демоэкзамена «Книжный магазин».

> Полная документация: [../README.md](../README.md) · [Развёртывание](../docs/DEPLOYMENT.md) · [Модули ТЗ](../docs/)

## Стек

Java 21 · Spring Boot 4.0.6 · JPA · SQLite · Spring Security · SpringDoc OpenAPI

## Быстрый старт

```powershell
.\gradlew.bat bootRun
```

**Данные:** положите xlsx в `input/` — импорт при пустой БД автоматически.  
Альтернатива: `python scripts\seed_db.py` (папка `test_files/` в корне репозитория).

Swagger: http://localhost:8082/api/v3/swagger-ui/index.html

## API

| Метод | URL | Доступ |
|-------|-----|--------|
| POST | `/api/auth/login` | все |
| GET | `/api/products` | все |
| GET/POST/PUT/DELETE | `/api/products/**` | admin |
| GET/POST/PUT/DELETE | `/api/orders/**` | manager/admin (GET), admin (CRUD) |
| GET | `/api/files/images/{name}` | все |

## Структура

```
domain/       User, Product, Order
repository/   Spring Data JPA
service/      бизнес-логика
web/          controllers, DTO
config/       Security, OpenAPI
storage/      SQLite + images
input/        xlsx и фото для автоимпорта
importer/     ExamDataImportService, парсеры xlsx
```

## Модель

3 сущности: `users`, `products`, `orders`. Подробнее: [docs/MODULE_1.md](../docs/MODULE_1.md)
