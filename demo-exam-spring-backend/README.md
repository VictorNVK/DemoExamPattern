# Demo Exam Spring Backend

REST API для демоэкзамена «Книжный магазин».

> Полная документация: [../README.md](../README.md) · [Развёртывание](../docs/DEPLOYMENT.md) · [Модули ТЗ](../docs/)

## Стек

Java 21 · Spring Boot 4.0.6 · JPA · SQLite · Spring Security · SpringDoc OpenAPI

## Быстрый старт

```powershell
.\gradlew.bat bootRun
python scripts\seed_db.py   # демо-данные (после первого запуска)
```

Swagger: http://localhost:8082/api/v3/swagger-ui/index.html

## API

| Метод | URL | Доступ |
|-------|-----|--------|
| POST | `/api/auth/login` | все |
| GET | `/api/products` | все |
| GET/POST/PUT/DELETE | `/api/products/**` | admin |
| GET | `/api/orders` | manager, admin |
| GET | `/api/files/images/{name}` | все |

## Структура

```
domain/       User, Product, Order
repository/   Spring Data JPA
service/      бизнес-логика
web/          controllers, DTO
config/       Security, OpenAPI
storage/      SQLite + images
```

## Модель

3 сущности: `users`, `products`, `orders`. Подробнее: [docs/MODULE_1.md](../docs/MODULE_1.md)
