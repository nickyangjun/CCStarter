#!/usr/bin/env bash
# 构建 sample JAR 并 Docker 部署（本地验证用）

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

require_cmd docker
BUILD_FIRST="${BUILD_FIRST:-1}"
IMAGE_TAG="${IMAGE_TAG:-ccstarter/sample-boot-app:local}"
COMPOSE_FILE="${ROOT_DIR}/deploy/sample/docker-compose.yml"

if [[ "${BUILD_FIRST}" == "1" ]]; then
  "${SCRIPT_DIR}/build.sh"
fi

JAR="$(find_sample_jar)"
STAGING="${ROOT_DIR}/deploy/sample/build"
mkdir -p "${STAGING}"
cp -f "${JAR}" "${STAGING}/app.jar"

log_info "构建镜像 ${IMAGE_TAG}"
docker build -t "${IMAGE_TAG}" "${ROOT_DIR}/deploy/sample"

log_info "启动容器（docker compose）"
docker compose -f "${COMPOSE_FILE}" up -d --build

PORT="${SMOKE_PORT:-18080}"
log_info "等待健康检查..."
SMOKE_BASE_URL="http://127.0.0.1:${PORT}" "${SCRIPT_DIR}/test-sample.sh"

log_info "部署完成。查看日志: docker compose -f ${COMPOSE_FILE} logs -f"
log_info "停止: docker compose -f ${COMPOSE_FILE} down"
