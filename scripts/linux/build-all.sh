#!/usr/bin/env bash
# Сборка backend и client.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_directory "$BACKEND_DIR"
require_directory "$CLIENT_DIR"
ensure_gradlew "$BACKEND_DIR"
ensure_gradlew "$CLIENT_DIR"

log "Сборка backend ..."
(
  cd "$BACKEND_DIR"
  ./gradlew build --no-daemon -x test
)

log "Сборка client ..."
(
  cd "$CLIENT_DIR"
  ./gradlew compileKotlin --no-daemon
)

log "Сборка завершена."
