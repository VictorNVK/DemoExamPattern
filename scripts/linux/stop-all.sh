#!/usr/bin/env bash
# Stop backend and client.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

stop_backend
log "Backend stopped."

stop_client
log "Done."
