#!/usr/bin/env bash
# Права на gradlew + загрузка Gradle-зависимостей backend и client.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command java
require_directory "$BACKEND_DIR"
require_directory "$CLIENT_DIR"

ensure_gradlew "$BACKEND_DIR"
ensure_gradlew "$CLIENT_DIR"
ensure_storage_dirs

log "Загрузка зависимостей backend ..."
(
  cd "$BACKEND_DIR"
  ./gradlew dependencies --no-daemon >/dev/null
  ./gradlew compileJava --no-daemon
)

log "Загрузка зависимостей client ..."
(
  cd "$CLIENT_DIR"
  ./gradlew dependencies --no-daemon >/dev/null
  ./gradlew compileKotlin --no-daemon
)

log "Проект подготовлен."
log "Дальше: ./scripts/linux/init-database.sh && ./scripts/linux/run-all.sh"
