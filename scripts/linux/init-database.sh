#!/usr/bin/env bash
# Создание SQLite и загрузка демо-данных.
#
# RESET_DB=1  — удалить старый файл БД перед инициализацией
# KEEP_BACKEND=1 — не останавливать backend после seed (по умолчанию backend остаётся запущенным)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command python3
require_command curl

if [[ "${RESET_DB:-0}" == "1" ]]; then
  log "RESET_DB=1 — остановка backend и удаление БД ..."
  stop_backend
  rm -f "$BACKEND_DB"
fi

ensure_storage_dirs

if [[ ! -f "$BACKEND_DB" ]]; then
  log "БД не найдена — запуск backend для создания схемы ..."
  start_backend_background
else
  log "БД уже существует: ${BACKEND_DB}"
  if ! is_backend_running; then
    start_backend_background
  fi
fi

"${SCRIPT_DIR}/seed-database.sh"

log "База данных готова."
