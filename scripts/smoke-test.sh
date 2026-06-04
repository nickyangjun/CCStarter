#!/usr/bin/env bash
# 一键全流程：编译 + 后台启动 sample + 冒烟 + 停止（CI / 提交前）
#
# 仅测已启动实例请用: ./scripts/test-sample.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"
# shellcheck source=lib/smoke-cases.sh
source "${SCRIPT_DIR}/lib/smoke-cases.sh"

require_cmd curl
require_cmd python3

BUILD_FIRST="${BUILD_FIRST:-1}"
PORT="${SMOKE_PORT:-18080}"
export SMOKE_BASE_URL="${SMOKE_BASE_URL:-http://127.0.0.1:${PORT}}"
export SMOKE_TRACE_ID="${SMOKE_TRACE_ID:-${DEFAULT_TRACE_ID}}"
export SMOKE_LOG="${SMOKE_LOG:-${ROOT_DIR}/.smoke/sample-boot-app.log}"
export SMOKE_LOG_CHECK="${SMOKE_LOG_CHECK:-1}"
APP_PID=""

cleanup() {
  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
    log_info "停止 sample 进程 ${APP_PID}"
    kill "${APP_PID}" 2>/dev/null || true
    wait "${APP_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

start_sample_background() {
  stop_sample_on_port "${PORT}"
  mkdir -p "$(dirname "${SMOKE_LOG}")"
  : > "${SMOKE_LOG}"
  JAR="$(find_sample_jar)"
  log_info "后台启动 sample: ${JAR}"
  java -jar "${JAR}" \
    --server.port="${PORT}" \
    --logging.level.com.company.component.log.request=DEBUG \
    >>"${SMOKE_LOG}" 2>&1 &
  APP_PID=$!
  wait_for_health "${SMOKE_BASE_URL}"
}

main() {
  if [[ "${BUILD_FIRST}" == "1" ]]; then
    "${SCRIPT_DIR}/build.sh"
  fi

  start_sample_background
  run_smoke_cases
}

main "$@"
