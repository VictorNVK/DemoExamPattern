#!/usr/bin/env bash
# Запуск Compose Desktop клиента.
#
# WAIT_BACKEND=0 — не ждать backend (по умолчанию ждём до 30 сек)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_directory "$CLIENT_DIR"
ensure_gradlew "$CLIENT_DIR"

if [[ "${WAIT_BACKEND:-1}" == "1" ]]; then
  if ! is_backend_running; then
    log "Backend не запущен. Пробую поднять в фоне ..."
    start_backend_background
  else
    log "Backend доступен: ${BACKEND_URL}"
  fi
fi

log "Запуск клиента (Ctrl+C для выхода) ..."
log "API: $(grep -E '^api.base_url=' "${CLIENT_DIR}/src/main/resources/application.properties" || echo 'http://localhost:8080')"
cd "$CLIENT_DIR"
exec ./gradlew run --no-daemon
