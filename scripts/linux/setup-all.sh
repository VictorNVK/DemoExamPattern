#!/usr/bin/env bash
# Полная первичная настройка: зависимости Gradle + БД + запуск.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/setup-project.sh"
RESET_DB="${RESET_DB:-1}" "${SCRIPT_DIR}/init-database.sh"

log "Можно запускать: ./scripts/linux/run-all.sh"
