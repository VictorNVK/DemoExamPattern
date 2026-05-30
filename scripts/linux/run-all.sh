#!/usr/bin/env bash
# Backend в фоне + клиент на переднем плане.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

if ! is_backend_running; then
  start_backend_background
else
  log "Backend уже запущен."
fi

"${SCRIPT_DIR}/run-client.sh"
