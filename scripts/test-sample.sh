#!/usr/bin/env bash
# 仅对「已启动」的 sample 做 HTTP 冒烟（不编译、不启动进程）
#
# 典型用法：
#   终端 1: ./scripts/run-sample.sh
#   终端 2: ./scripts/test-sample.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"
# shellcheck source=lib/smoke-cases.sh
source "${SCRIPT_DIR}/lib/smoke-cases.sh"

require_cmd curl
require_cmd python3

PORT="${SMOKE_PORT:-18080}"
export SMOKE_BASE_URL="${SMOKE_BASE_URL:-http://127.0.0.1:${PORT}}"
export SMOKE_TRACE_ID="${SMOKE_TRACE_ID:-${DEFAULT_TRACE_ID}}"
export SMOKE_LOG="${SMOKE_LOG:-}"

log_info "目标: ${SMOKE_BASE_URL}"
log_info "网关模拟 TraceId（用例 1/3+）: ${SMOKE_TRACE_ID}"
log_info "用例 2 将不带 X-Trace-Id，验证服务自建 traceId"
log_info "请先确保已执行: ./scripts/run-sample.sh（或其它方式启动 sample）"

wait_for_health "${SMOKE_BASE_URL}" 15

run_smoke_cases
