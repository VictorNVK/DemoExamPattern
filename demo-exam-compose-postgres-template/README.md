# Compose Desktop Client

Desktop-клиент демоэкзамена «Книжный магазин». Работает через REST API backend.

> Полная документация: [../README.md](../README.md) · [Развёртывание](../docs/DEPLOYMENT.md) · [Модули ТЗ](../docs/)

## Быстрый старт

```powershell
# Сначала запустите backend (см. demo-exam-spring-backend)
.\gradlew.bat run
```

## Конфигурация API

`src/main/resources/application.properties`:

```properties
app.title=Book Store Demo Template
app.company_name=Book Store
api.base_url=http://localhost:8080

api.endpoint.auth.login=/api/auth/login
api.endpoint.products=/api/products
api.endpoint.products.options=/api/products/options
api.endpoint.products.next_id=/api/products/next-id
api.endpoint.orders=/api/orders
api.endpoint.files.images=/api/files/images
```

Класс `config/BackendApiConfig.kt` собирает полные URL.  
Переопределение через env: `API_BASE_URL`, `API_ENDPOINT_PRODUCTS`, …

## Структура

```
app/          AppState, навигация
api/          BackendClient (Ktor)
config/       AppConfig, BackendApiConfig
data/         репозитории (REST)
ui/screens/   Login, Products, Editor, Orders
model/        UserRole, Product, Order
```

## Роли (UI)

| Роль | Каталог | Поиск | Заказы | CRUD |
|------|---------|-------|--------|------|
| Гость / Client | ✓ | — | — | — |
| Manager | ✓ | ✓ | ✓ | — |
| Admin | ✓ | ✓ | ✓ | ✓ |

Подробнее: [docs/MODULE_2.md](../docs/MODULE_2.md) … [MODULE_4.md](../docs/MODULE_4.md)

## Артефакты для экзамена

`docs/er/`, `docs/screenshots/`, `docs/algorithms/` — см. [docs/README.md](docs/README.md)
