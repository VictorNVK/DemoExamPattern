#!/usr/bin/env bash
# Загрузка тестовых данных в SQLite (users, products, orders).
# База должна уже существовать (схему создаёт Hibernate при первом запуске backend).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command python3
require_directory "$BACKEND_DIR"

if [[ ! -f "$BACKEND_DB" ]]; then
  die "Файл БД не найден: ${BACKEND_DB}. Сначала запустите: ./scripts/linux/init-database.sh"
fi

log "Загрузка данных в SQLite ..."
python3 "${BACKEND_DIR}/scripts/seed_db.py"

log "Данные загружены."
print_credentials
