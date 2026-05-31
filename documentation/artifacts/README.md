# Папки для артефактов сдачи (по ТЗ)

Положите файлы в указанные каталоги перед загрузкой в репозиторий.

| Модуль | Артефакт | Формат | Путь |
|--------|----------|--------|------|
| М1 | ER-диаграмма | PDF | `demo-exam-compose-postgres-template/docs/er/` |
| М1 | Скрипт БД | `.py` / SQL | `demo-exam-spring-backend/scripts/seed_db.py` ✅ |
| М2 | Блок-схема алгоритма | PDF (ГОСТ 19.701-90) | `demo-exam-compose-postgres-template/docs/algorithms/` |
| М2 | Скриншоты работы | DOCX + PNG | `demo-exam-compose-postgres-template/docs/screenshots/` |
| М3–М4 | Скриншоты CRUD | PNG (в DOCX или отдельно) | `docs/screenshots/` |
| Все | Исходный код | не архив | корень репозитория ✅ |
| Все | Исполняемые файлы | JAR / пакет Compose | см. DEPLOYMENT.md |

## Что уже есть в репозитории

- ✅ Исходный код backend + client
- ✅ `seed_db.py`
- ✅ `documentation/modules/` — описание по модулям
- ✅ `docs/DEPLOYMENT.md` — развёртывание

## Что нужно добавить вручную

- ❌ ER-диаграмма PDF
- ❌ Блок-схема PDF
- ❌ DOCX со скриншотами (модуль 2)
