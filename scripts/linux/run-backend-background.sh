#!/usr/bin/env bash
# Запуск backend в фоне.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

start_backend_background
log "Лог: ${BACKEND_LOG_FILE}"
log "Swagger: ${BACKEND_URL}/api/v3/swagger-ui/index.html"
print_credentials
