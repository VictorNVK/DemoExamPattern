#!/usr/bin/env bash
# Установка системных зависимостей (Ubuntu / Debian).
# Для других дистрибутивов установите аналоги пакетов вручную.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

if ! command -v apt-get >/dev/null 2>&1; then
  cat <<'EOF'
Скрипт рассчитан на Ubuntu / Debian (apt-get).

Минимально необходимо:
  - JDK 21 (backend Spring Boot)
  - JDK 17+ (клиент Compose Desktop; подойдёт JDK 21)
  - Python 3 (загрузка SQLite)
  - curl, git, unzip
  - библиотеки для Compose Desktop (libx11, libgtk-3, fontconfig, ...)

EOF
  exit 1
fi

log "Обновление списка пакетов ..."
sudo apt-get update

log "Установка базовых утилит и JDK ..."
sudo apt-get install -y \
  git \
  curl \
  wget \
  unzip \
  zip \
  xz-utils \
  file \
  ca-certificates \
  build-essential \
  pkg-config \
  python3 \
  openjdk-21-jdk

log "Установка библиотек для Compose Desktop ..."
sudo apt-get install -y \
  fontconfig \
  libfreetype6 \
  libx11-6 \
  libxext6 \
  libxrender1 \
  libxtst6 \
  libxi6 \
  libxrandr2 \
  libxcursor1 \
  libxdamage1 \
  libxfixes3 \
  libxinerama1 \
  libgl1 \
  libglu1-mesa \
  libgtk-3-0 \
  libnss3 \
  libgbm1 \
  libatk-bridge2.0-0 \
  libdrm2 \
  libasound2t64 2>/dev/null || sudo apt-get install -y libasound2

log "Опционально: инструменты для сборки .deb пакета клиента ..."
sudo apt-get install -y fakeroot dpkg-dev || true

log "Проверка версий:"
java -version || true
python3 --version || true

log "Готово. Дальше: ./scripts/linux/setup-project.sh"
