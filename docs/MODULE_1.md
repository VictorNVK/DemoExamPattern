# Модуль 1. Проектирование и создание базы данных

Соответствует заданию **М1** демоэкзамена (КОД 09.02.07-2-2026): разработка базы данных для предметной области «Книжный магазин».

## Цель модуля

- Спроектировать структуру БД по предметной области.
- Создать базу данных и подключить её к приложению.
- Обеспечить отображение данных из БД в интерфейсе.

## Реализация в проекте

База данных находится на **backend** (клиент БД не использует).

| Параметр | Значение |
|----------|----------|
| СУБД | SQLite 3 |
| Файл | `demo-exam-spring-backend/storage/database/book_store_exam.db` |
| ORM | Hibernate / Spring Data JPA |
| Создание схемы | автоматически (`spring.jpa.hibernate.ddl-auto=update`) |

## Модель данных (3 таблицы)

Упрощённая модель без отдельных справочников — поля категории, производителя, поставщика хранятся строками в таблице товаров (достаточно для ТЗ).

### `users` — пользователи

| Поле | Тип | Описание |
|------|-----|----------|
| id | INTEGER PK | Идентификатор |
| full_name | TEXT | ФИО |
| login | TEXT | Логин |
| password | TEXT | Пароль |
| role | TEXT | Роль: `client`, `manager`, `admin` |
| active | BOOLEAN | Активен |

### `products` — товары

| Поле | Тип | Описание |
|------|-----|----------|
| id | INTEGER PK | Артикул/ID товара |
| article | TEXT | Внутренний артикул |
| name | TEXT | Наименование |
| category | TEXT | Категория |
| description | TEXT | Описание |
| manufacturer | TEXT | Производитель |
| supplier | TEXT | Поставщик |
| unit | TEXT | Единица измерения |
| price | DECIMAL | Цена |
| stock_quantity | INTEGER | Остаток на складе |
| discount_percent | DECIMAL | Скидка, % |
| image_path | TEXT | Имя файла изображения |
| created_at, updated_at | DATETIME | Служебные даты |

### `orders` — заказы

| Поле | Тип | Описание |
|------|-----|----------|
| id | INTEGER PK | Номер заказа |
| customer_name | TEXT | Клиент |
| manager_id | FK → users | Менеджер |
| status | TEXT | Статус |
| pickup_address | TEXT | Адрес пункта выдачи |
| order_date, delivery_date | DATETIME | Даты |
| pickup_code | TEXT | Код получения |
| comment | TEXT | Комментарий |
| product_id | FK → products | Товар в заказе |
| quantity | INTEGER | Количество |
| unit_price | DECIMAL | Цена за единицу |
| discount_percent | DECIMAL | Скидка |

## JPA-сущности (backend)

```
demo-exam-spring-backend/src/main/java/ru/demoexam/backend/domain/
├── User.java
├── Product.java
└── Order.java
```

Репозитории: `UserRepository`, `ProductRepository`, `OrderRepository`.

## Подключение к приложению

Конфигурация: `demo-exam-spring-backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:sqlite:storage/database/book_store_exam.db?foreign_keys=on
spring.jpa.hibernate.ddl-auto=update
```

При первом запуске backend создаёт каталог `storage/database/` и файл БД.

## Загрузка тестовых данных

```powershell
cd demo-exam-spring-backend
python scripts/seed_db.py
```

Скрипт заполняет 3 пользователя, 4 товара, 2 заказа.

## Артефакты для сдачи (по ТЗ)

| Артефакт | Где разместить |
|----------|----------------|
| ER-диаграмма (PDF) | `demo-exam-compose-postgres-template/docs/er/` |
| Скрипт БД | `demo-exam-spring-backend/scripts/seed_db.py` + схема через JPA |
| Скриншоты | `demo-exam-compose-postgres-template/docs/screenshots/` |

## Проверка

1. Запустить backend: `.\gradlew.bat bootRun`
2. Открыть Swagger: http://localhost:8082/api/v3/swagger-ui/index.html
3. `GET /api/products` — список товаров из БД
4. `POST /api/auth/login` — авторизация по данным из `users`
