# Полное развёртывание проекта

Инструкция по установке, настройке и запуску демоэкзамена «Книжный магазин» (КОД 09.02.07-2-2026).

## 1. Состав проекта

```
Демо экзамен файлы/
├── demo-exam-spring-backend/           # Java backend + SQLite
├── demo-exam-compose-postgres-template/ # Kotlin Compose client
├── docs/                               # документация по модулям ТЗ
└── scripts/linux/                      # скрипты для Linux
```

**Не требуется:** PostgreSQL, Docker, отдельный сервер БД.

## 2. Системные требования

| Компонент | Минимум |
|-----------|---------|
| ОС | Windows 10+ / Ubuntu 20.04+ / Debian 11+ |
| RAM | 4 GB |
| Диск | 2 GB свободно (с Gradle cache) |
| JDK | 21 (backend), 17+ (client) |
| Python 3 | для загрузки демо-данных (опционально) |

## 2.1. Как поднимается база данных

SQLite **не запускается отдельно** — это файл на диске.

| Шаг | Что происходит |
|-----|----------------|
| `bootRun` | Создаются папки `storage/database/`, `storage/images/` |
| Hibernate (`ddl-auto=update`) | Создаёт/обновляет `storage/database/book_store_exam.db` и таблицы |
| `python scripts/seed_db.py` | Заполняет users, products, orders (демо-данные) |

Порт backend по умолчанию: **8082** (`server.port` в `application.properties`).  
Клиент: `api.base_url=http://localhost:8082`.

## 3. Windows — установка и запуск

### 3.1. Установка JDK 21

1. Скачать [OpenJDK 21](https://adoptium.net/) или использовать JetBrains JDK.
2. Проверка:
   ```powershell
   java -version
   ```

### 3.2. Клонирование / распаковка

Скопируйте папку проекта, например:
```
C:\Users\...\Демо экзамен файлы\
```

### 3.3. Загрузка Gradle-зависимостей

```powershell
cd "C:\Users\...\Демо экзамен файлы\demo-exam-spring-backend"
.\gradlew.bat compileJava

cd "..\demo-exam-compose-postgres-template"
.\gradlew.bat compileKotlin
```

### 3.4. Запуск backend

**Терминал 1:**
```powershell
cd demo-exam-spring-backend
.\gradlew.bat bootRun
```

Дождитесь строки:
```
Started DemoExamBackendApplication
```

Swagger: http://localhost:8082/api/v3/swagger-ui/index.html

> **Ошибка «Port … was already in use»** — backend уже запущен. Либо используйте его, либо остановите:
> ```powershell
> netstat -ano | findstr :8082
> taskkill /PID <номер_процесса> /F
> ```
> Или из корня репозитория: `.\scripts\windows\stop-backend.ps1`

### 3.5. Загрузка демо-данных в SQLite

**Терминал 2** (backend должен был хотя бы раз стартовать — создастся файл БД):

```powershell
cd demo-exam-spring-backend
python scripts\seed_db.py
```

Файл БД: `storage/database/book_store_exam.db`

Пересоздание данных (очистка и повторная загрузка):
```powershell
python scripts\seed_db.py
```

Полное пересоздание БД: удалите `storage/database/book_store_exam.db`, снова запустите `bootRun`, затем `seed_db.py`.

### 3.6. Запуск клиента

**Терминал 2** (или 3, если backend в первом):

```powershell
cd demo-exam-compose-postgres-template
.\gradlew.bat run
```

### 3.7. Настройка URL backend (если не localhost:8082)

**Backend** — `demo-exam-spring-backend/src/main/resources/application.properties`:
```properties
server.port=8082
```

**Client** — `demo-exam-compose-postgres-template/src/main/resources/application.properties`:
```properties
api.base_url=http://localhost:8082
```

Или переменная окружения:
```powershell
$env:API_BASE_URL="http://192.168.1.10:8082"
.\gradlew.bat run
```

## 4. Linux — установка и запуск

### 4.1. Автоматическая установка (Ubuntu/Debian)

Из корня репозитория:

```bash
chmod +x scripts/linux/*.sh

# Системные пакеты (JDK, Python, библиотеки Compose)
./scripts/linux/install-deps.sh

# Gradle-зависимости + компиляция
./scripts/linux/setup-project.sh

# SQLite: схема + демо-данные
./scripts/linux/init-database.sh

# Backend (фон) + клиент
./scripts/linux/run-all.sh
```

Краткая альтернатива:
```bash
./scripts/linux/setup-all.sh
./scripts/linux/run-all.sh
```

Подробнее: [scripts/linux/README.md](../scripts/linux/README.md)

### 4.2. Ручной запуск (Linux)

```bash
# Backend
cd demo-exam-spring-backend
./gradlew bootRun

# Client (другой терминал)
cd demo-exam-compose-postgres-template
./gradlew run
```

### 4.3. Остановка backend (Linux)

```bash
./scripts/linux/stop-backend.sh
```

## 5. Проверка работоспособности

### 5.1. Swagger

1. http://localhost:8082/api/v3/swagger-ui/index.html
2. `POST /api/auth/login` → body: `{"login":"admin","password":"admin"}`
3. **Authorize** → `Bearer {token}`
4. `GET /api/products`, `GET /api/orders`

### 5.2. Скрипт проверки (Python)

```powershell
# Windows
python demo-exam-spring-backend\scripts\check_api.py
```

```bash
# Linux
./scripts/linux/check-api.sh
```

### 5.3. Клиент

| Действие | Ожидание |
|----------|----------|
| Гость | Каталог без фильтров |
| client/client | Каталог |
| manager/manager | Каталог + фильтры + заказы |
| admin/admin | CRUD товаров + всё выше |

## 6. Сборка production-артефактов

### Backend (JAR)

```powershell
cd demo-exam-spring-backend
.\gradlew.bat bootJar
```

JAR: `build/libs/demo-exam-spring-backend-1.0.0.jar`

Запуск:
```powershell
java -jar build/libs/demo-exam-spring-backend-1.0.0.jar
```

### Client (дистрибутив)

```powershell
cd demo-exam-compose-postgres-template
.\gradlew.bat packageDistributionForCurrentOS
```

Пакет появится в `build/compose/binaries/`.

## 7. Структура данных на диске

```
demo-exam-spring-backend/
├── storage/
│   ├── database/
│   │   └── book_store_exam.db    # SQLite
│   ├── images/
│   │   └── product_*.png       # фото товаров
│   ├── backend.log             # лог (Linux, фоновый запуск)
│   └── backend.pid
└── scripts/
    ├── seed_db.py              # демо-данные
    └── check_api.py            # проверка API
```

## 8. Учётные записи по умолчанию

| Логин | Пароль | Роль |
|-------|--------|------|
| client | client | client |
| manager | manager | manager |
| admin | admin | admin |

## 9. Переменные окружения

### Backend

| Переменная | Свойство | По умолчанию |
|------------|----------|--------------|
| — | `server.port` | 8082 |
| — | `app.storage-root` | storage |

### Client

| Переменная | Свойство | По умолчанию |
|------------|----------|--------------|
| `APP_TITLE` | app.title | Book Store Demo Template |
| `APP_COMPANY_NAME` | app.company_name | Book Store |
| `API_BASE_URL` | api.base_url | http://localhost:8082 |
| `API_ENDPOINT_PRODUCTS` | api.endpoint.products | /api/products |

Полный список эндпоинтов: `BackendApiConfig.kt`.

## 10. Устранение неполадок

| Проблема | Решение |
|----------|---------|
| Порт backend занят | `netstat -ano | findstr :8082` или `.\scripts\windows\stop-backend.ps1` |
| Клиент не подключается | Проверить `api.base_url`, backend запущен |
| Пустой каталог | Выполнить `seed_db.py` |
| 401 Unauthorized | Войти через клиент или получить token в Swagger |
| 403 Forbidden | Неверная роль (CRUD — только admin) |
| Нет фото | Файл в `storage/images/`, путь в `products.image_path` |
| Gradle медленный | Первый запуск скачивает зависимости — это нормально |
| Database is locked (IntelliJ) | Остановите `bootRun` — SQLite занят backend |

## 10.1. Просмотр SQLite в IntelliJ IDEA

Файл БД:
```
demo-exam-spring-backend/storage/database/book_store_exam.db
```

### IntelliJ IDEA Ultimate

1. **View → Tool Windows → Database**
2. **+ → Data Source → SQLite**
3. **File** — укажите путь к `book_store_exam.db`
4. **Download missing driver files** (если предложит)
5. **Test Connection → OK**
6. Таблицы: `users`, `products`, `orders`

### IntelliJ IDEA Community

Плагин **Database Navigator** или внешний **DB Browser for SQLite**.

### JDBC URL (если нужен)

```
jdbc:sqlite:C:/.../demo-exam-spring-backend/storage/database/book_store_exam.db
```

> Пока работает `bootRun`, файл может быть заблокирован — для просмотра/редактирования в IDEA остановите backend.

## 11. Документация по модулям ТЗ

| Модуль | Файл |
|--------|------|
| М1 — БД | [MODULE_1.md](MODULE_1.md) |
| М2 — Авторизация и каталог | [MODULE_2.md](MODULE_2.md) |
| М3 — CRUD товаров | [MODULE_3.md](MODULE_3.md) |
| М4 — Заказы | [MODULE_4.md](MODULE_4.md) |

## 12. Сдача на экзамене

По ТЗ в репозиторий загружаются:

- исходный код (структура с файлами, не архив);
- исполняемые файлы / инструкция запуска (этот документ);
- скрипт БД (`scripts/seed_db.py` + описание схемы в MODULE_1);
- ER-диаграмма, скриншоты — в `demo-exam-compose-postgres-template/docs/`.
