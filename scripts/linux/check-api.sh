#!/usr/bin/env bash
# Проверка API после запуска backend.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

require_command python3

if ! is_backend_running; then
  die "Backend не запущен. Выполните: ./scripts/linux/run-backend-background.sh"
fi

python3 "${BACKEND_DIR}/scripts/check_api.py"
