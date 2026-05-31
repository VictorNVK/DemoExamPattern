#!/usr/bin/env bash
# Запуск Spring Boot backend (foreground).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_directory "$BACKEND_DIR"
ensure_gradlew "$BACKEND_DIR"
ensure_storage_dirs

if is_backend_running; then
  log "Backend уже запущен: ${BACKEND_URL}"
  log "Swagger: ${BACKEND_URL}/api/v3/swagger-ui/index.html"
  exit 0
fi

log "Запуск backend (Ctrl+C для остановки) ..."
log "Swagger: ${BACKEND_URL}/swagger-ui"
cd "$BACKEND_DIR"
exec ./gradlew bootRun --no-daemon
