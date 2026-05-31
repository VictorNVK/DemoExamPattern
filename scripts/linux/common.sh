#!/usr/bin/env bash
# Общие переменные и функции для скриптов demo-exam (Linux).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

BACKEND_DIR="${ROOT_DIR}/demo-exam-spring-backend"
CLIENT_DIR="${ROOT_DIR}/demo-exam-compose-postgres-template"
BACKEND_DB="${BACKEND_DIR}/storage/database/book_store_exam.db"
BACKEND_PORT="${BACKEND_PORT:-8082}"
BACKEND_URL="http://localhost:${BACKEND_PORT}"
BACKEND_PID_FILE="${BACKEND_DIR}/storage/backend.pid"
BACKEND_LOG_FILE="${BACKEND_DIR}/storage/backend.log"

log() {
  printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

read_backend_port() {
  local props="${BACKEND_DIR}/src/main/resources/application.properties"
  if [[ ! -f "$props" ]]; then
    return 0
  fi

  local port
  port="$(grep -E '^[[:space:]]*server\.port[[:space:]]*=' "$props" | tail -n1 | sed -E 's/^[^=]*=[[:space:]]*([0-9]+).*/\1/')"
  if [[ -n "$port" ]]; then
    BACKEND_PORT="$port"
    BACKEND_URL="http://localhost:${BACKEND_PORT}"
  fi
}

die() {
  log "ERROR: $*"
  exit 1
}

require_command() {
  local name="$1"
  command -v "$name" >/dev/null 2>&1 || die "Команда '${name}' не найдена. Запустите: ./scripts/linux/install-deps.sh"
}

require_directory() {
  local path="$1"
  [[ -d "$path" ]] || die "Каталог не найден: ${path}"
}

ensure_gradlew() {
  local project_dir="$1"
  local gradlew="${project_dir}/gradlew"
  [[ -x "$gradlew" ]] || chmod +x "$gradlew"
}

ensure_storage_dirs() {
  mkdir -p "${BACKEND_DIR}/storage/database"
  mkdir -p "${BACKEND_DIR}/storage/images"
}

is_backend_running() {
  if [[ -f "$BACKEND_PID_FILE" ]]; then
    local pid
    pid="$(cat "$BACKEND_PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi

  if command -v curl >/dev/null 2>&1; then
    curl -fsS "${BACKEND_URL}/api/v3/api-docs" >/dev/null 2>&1
    return $?
  fi

  return 1
}

wait_for_backend() {
  local attempts="${1:-60}"
  local delay="${2:-2}"

  log "Ожидание backend на ${BACKEND_URL} ..."
  for ((i = 1; i <= attempts; i++)); do
    if curl -fsS "${BACKEND_URL}/api/v3/api-docs" >/dev/null 2>&1; then
      log "Backend доступен."
      return 0
    fi
    sleep "$delay"
  done

  die "Backend не ответил за $((attempts * delay)) сек. Смотрите лог: ${BACKEND_LOG_FILE}"
}

stop_backend() {
  read_backend_port

  if [[ -f "$BACKEND_PID_FILE" ]]; then
    local pid
    pid="$(cat "$BACKEND_PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      log "Stopping backend (PID ${pid}) ..."
      kill "$pid" 2>/dev/null || true
      sleep 2
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$BACKEND_PID_FILE"
  fi

  if command -v fuser >/dev/null 2>&1; then
    fuser -k "${BACKEND_PORT}/tcp" >/dev/null 2>&1 || true
  elif command -v lsof >/dev/null 2>&1; then
    local pids
    pids="$(lsof -ti tcp:"${BACKEND_PORT}" 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
      log "Stopping backend on port ${BACKEND_PORT} ..."
      kill $pids 2>/dev/null || true
    fi
  fi

  if command -v pkill >/dev/null 2>&1; then
    pkill -f "demo-exam-spring-backend" >/dev/null 2>&1 || true
    pkill -f "DemoExamBackendApplication" >/dev/null 2>&1 || true
  fi
}

stop_client() {
  if command -v pkill >/dev/null 2>&1; then
    pkill -f "demo-exam-compose-postgres-template" >/dev/null 2>&1 || true
    pkill -f "ru.demoexam.template.MainKt" >/dev/null 2>&1 || true
    pkill -f "DemoExamTemplate" >/dev/null 2>&1 || true
    log "Client stop signal sent."
    return 0
  fi

  log "pkill not found; client must be closed manually."
}

start_backend_background() {
  if is_backend_running; then
    log "Backend уже запущен."
    return 0
  fi

  require_directory "$BACKEND_DIR"
  ensure_gradlew "$BACKEND_DIR"
  ensure_storage_dirs

  log "Запуск backend в фоне ..."
  (
    cd "$BACKEND_DIR"
    nohup ./gradlew bootRun --no-daemon >"$BACKEND_LOG_FILE" 2>&1 &
    echo $! >"$BACKEND_PID_FILE"
  )

  wait_for_backend
}

print_credentials() {
  cat <<'EOF'

Тестовые пользователи (после seed):
  client  / client   — клиент
  manager / manager  — менеджер
  admin   / admin    — администратор

Swagger UI:
  ${BACKEND_URL}/api/v3/swagger-ui/index.html

EOF
}
