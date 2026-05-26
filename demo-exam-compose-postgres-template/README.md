# Шаблон для демоэкзамена: Kotlin Compose Desktop + Room

Это стартовый шаблон desktop-приложения под демоэкзамен на `Kotlin Compose Desktop` с локальной базой `SQLite` через `Room`.
Главная цель шаблона: быстро делать экзаменационные CRUD-задачи без PostgreSQL, Docker и ручного JDBC-слоя.

Подробная инструкция по адаптации под разные варианты находится в [README_VARIANTS.md](README_VARIANTS.md).
Команды для Linux вынесены в [LINUX_INSTALL_COMMANDS.txt](LINUX_INSTALL_COMMANDS.txt).

## Что уже есть

- `Compose Desktop` приложение на Kotlin
- локальная БД `SQLite`
- `Room` как ORM-слой
- роли: гость, клиент, менеджер, администратор
- экран входа
- экран списка товаров
- поиск, фильтрация и сортировка
- форма добавления и редактирования товара
- удаление товара с проверкой связей
- экран заказов как основа под дальнейший CRUD
- импорт из `.xlsx`
- папка `storage/images` для изображений товаров
- папки `docs/` и `imports/` для материалов экзамена

## Что изменилось

- PostgreSQL удален
- Docker больше не нужен
- `Flyway`, `HikariCP` и `PostgreSQL JDBC` удалены
- данные хранятся в локальном файле SQLite
- структура БД описывается через `Room Entity/DAO/Relation`

## Где лежит база

После первого запуска создается файл:

- `storage/database/book_store_exam.db`

Имя файла можно поменять в `src/main/resources/application.properties` через `db.file_name`.

## Быстрый старт

### 1. Собрать проект

```powershell
.\gradlew.bat compileKotlin
```

### 2. Запустить приложение

```powershell
.\gradlew.bat run
```

### 3. Тестовые учетные записи

- `client / client`
- `manager / manager`
- `admin / admin`

При первом запуске база и стартовые данные создаются автоматически.

## Полезные команды

```powershell
.\gradlew.bat compileKotlin
.\gradlew.bat test
.\gradlew.bat run
.\gradlew.bat importXlsx -PimportDir="C:\path\to\xlsx"
.\gradlew.bat packageDistributionForCurrentOS
```

## Основные каталоги

- `src/main/kotlin` — основной код
- `src/main/kotlin/ru/demoexam/template/data/local` — `Room` сущности, связи и DAO
- `src/main/resources/assets` — логотип и заглушка изображения
- `imports` — входные файлы экзамена
- `docs/er` — ER-диаграммы
- `docs/algorithms` — блок-схемы
- `docs/screenshots` — скриншоты работы
- `storage/database` — файл SQLite базы
- `storage/images` — изображения товаров

## Импорт из XLSX

Шаблон импортирует типовые экзаменационные файлы:

- `Tovar.xlsx`
- `user_import.xlsx`
- `Заказ_import.xlsx`
- `Пункты выдачи_import.xlsx`

Запуск через Gradle:

```powershell
.\gradlew.bat importXlsx -PimportDir="C:\Users\Victor\Desktop\Демо экзамен файлы"
```

Запуск через PowerShell:

```powershell
.\scripts\import-xlsx.ps1
.\scripts\import-xlsx.ps1 -SourceDir "C:\Users\Victor\Desktop\Демо экзамен файлы"
```

Что делает импортёр:

- очищает предметные таблицы перед новой загрузкой
- импортирует пользователей, товары, пункты выдачи и заказы
- автоматически добавляет недостающие справочники
- копирует изображения товаров в `storage/images`
- сохраняет путь к изображению в БД
- разбирает состав заказа из ячейки Excel

## Конфигурация

Настройки читаются из `src/main/resources/application.properties`.
Поддерживаются параметры:

- `app.title`
- `app.company_name`
- `db.file_name`

Их можно переопределять переменными окружения:

- `APP_TITLE`
- `APP_COMPANY_NAME`
- `DB_FILE_NAME`

## Room и SQL

Шаблон специально переведен на `Room`, чтобы основную работу делать без ручного SQL-слоя:

- таблицы описываются через `@Entity`
- связи задаются через `@ForeignKey` и `@Relation`
- вставка, обновление и удаление идут через `@Insert`, `@Update`, `@Delete`
- выборки описаны в `@Dao`

Отдельный JDBC-слой и SQL-миграции больше не нужны.

## Замечания

- Для сборки и запуска лучше использовать `JDK 17`.
- Пароли в шаблоне хранятся в открытом виде, потому что это учебная заготовка.
- Если структура варианта изменилась, сначала меняй `Entity` и `DAO`, потом репозитории, потом UI.
