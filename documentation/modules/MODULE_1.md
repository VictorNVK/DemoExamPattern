# Модуль 1. Разработка базы данных средствами СУБД

**КОД 09.02.07-2-2026 · М1**

## Задание по ТЗ

1. Создать БД по предметной области (книжный магазин).
2. Обеспечить **3 нормальную форму** и **ссылочную целостность**.
3. Согласованное именование, PK/FK.
4. **ER-диаграмма** в PDF (таблицы, связи, атрибуты, ключи).
5. Импорт данных из файлов заказчика (Приложение 2, xlsx).
6. Сохранить **скрипт БД** или файл конфигурации с данными.
7. Подключить БД к приложению; данные отображаются в интерфейсе.

**Приложения ТЗ:** Прил_1_ОЗ_КОД 09.02.07-2-2026-М1.docx, Прил_2_ОЗ_КОД 09.02.07-2-2026-М1.rar

---

## Реализация в проекте

### СУБД и файл

| Параметр | Значение |
|----------|----------|
| СУБД | SQLite 3 |
| Файл | `demo-exam-spring-backend/storage/database/book_store_exam.db` |
| ORM | Hibernate / Spring Data JPA |
| Создание схемы | `spring.jpa.hibernate.ddl-auto=update` при `bootRun` |

### Таблицы

| Таблица | Назначение |
|---------|------------|
| `users` | Пользователи: ФИО, логин, пароль, роль, active |
| `products` | Товары: все реквизиты + image_path |
| `orders` | Заказы: клиент, менеджер (FK→users), товар (FK→products), статус, адрес, даты |

### JPA-сущности

```
demo-exam-spring-backend/src/main/java/ru/demoexam/backend/domain/
├── User.java
├── Product.java
└── Order.java
```

### Репозитории

- `UserRepository`
- `ProductRepository`
- `OrderRepository`

### Скрипт данных

`demo-exam-spring-backend/scripts/seed_db.py` — заполнение демо-данными (3 пользователя, 4 товара, 2 заказа).

Запуск (после первого `bootRun`):

```powershell
cd demo-exam-spring-backend
python scripts\seed_db.py
```

---

## Чек-лист соответствия ТЗ

| № | Требование | Статус | Комментарий |
|---|------------|--------|-------------|
| 1 | БД создана | ✅ | SQLite + Hibernate |
| 2 | 3НФ | ⚠️ | Упрощённая схема: справочники (категория, поставщик…) — строки в `products`, без отдельных таблиц |
| 3 | Ссылочная целостность | ✅ | FK orders→users, orders→products; `foreign_keys=on` |
| 4 | ER-диаграмма PDF | ❌ | **Нужно оформить** и положить в `docs/er/` |
| 5 | Импорт xlsx | ⚠️ | Вместо xlsx — `seed_db.py`; при необходимости восстановить импорт из Прил_2 |
| 6 | Скрипт БД | ✅ | `scripts/seed_db.py` |
| 7 | Подключение к приложению | ✅ | Клиент → REST API → JPA → SQLite |

---

## ER-схема (логическая)

```
users (id PK)
  │
  └──< orders.manager_id

products (id PK)
  │
  └──< orders.product_id

orders (id PK, manager_id FK, product_id FK)
```

Для сдачи оформите **визуальную ER-диаграмму** в draw.io / dbdiagram / IntelliJ Diagrams → экспорт **PDF**.

---

## Что сдать по модулю

1. PDF ER-диаграммы → `demo-exam-compose-postgres-template/docs/er/`
2. Скрипт `seed_db.py` (уже в репозитории)
3. Скриншот таблиц в IntelliJ Database / DB Browser (опционально)

---

## Проверка

1. `bootRun` backend → файл `.db` создан
2. `python scripts\seed_db.py`
3. IntelliJ: Database → SQLite → `book_store_exam.db` → таблицы с данными
4. Swagger: `GET /api/products` — товары из БД
