#!/usr/bin/env bash
# Docker 全栈部署（多阶段构建，无需本地 Maven）

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

require_cmd docker
COMPOSE_FILE="${ROOT_DIR}/deploy/docker/stack/docker-compose.yml"
RUN_SMOKE="${RUN_SMOKE:-1}"

log_info "构建并启动（容器内 Maven 编译：MySQL + Redis + sample，profile=docker,test）"
docker compose -f "${COMPOSE_FILE}" up -d --build

if [[ "${RUN_SMOKE}" == "1" ]]; then
  PORT="${SMOKE_PORT:-18080}"
  log_info "等待健康检查..."
  SMOKE_BASE_URL="http://127.0.0.1:${PORT}" "${SCRIPT_DIR}/test-sample.sh"
fi

log_info "部署完成。查看日志: docker compose -f ${COMPOSE_FILE} logs -f sample-boot-app"
log_info "停止: docker compose -f ${COMPOSE_FILE} down"
log_info "仅启动不冒烟: RUN_SMOKE=0 docker compose -f ${COMPOSE_FILE} up -d --build"
