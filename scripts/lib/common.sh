#!/usr/bin/env bash
# 公共函数：路径、日志、HTTP 断言

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SAMPLE_MODULE="company-component-samples/sample-boot-app"
SAMPLE_DIR="${ROOT_DIR}/${SAMPLE_MODULE}"
DEFAULT_PORT="${SMOKE_PORT:-18080}"
DEFAULT_BASE_URL="http://127.0.0.1:${DEFAULT_PORT}"
DEFAULT_TRACE_ID="${SMOKE_TRACE_ID:-smoke-trace-0001}"

log_info() { printf '\033[0;32m[INFO]\033[0m %s\n' "$*"; }
log_warn() { printf '\033[0;33m[WARN]\033[0m %s\n' "$*"; }
log_error() { printf '\033[0;31m[ERROR]\033[0m %s\n' "$*" >&2; }

die() {
  log_error "$@"
  exit 1
}

require_cmd() {
  local cmd="$1"
  command -v "$cmd" >/dev/null 2>&1 || die "缺少命令: $cmd（请先安装）"
}

find_sample_jar() {
  local jar
  jar="$(find "${SAMPLE_DIR}/target" -maxdepth 1 -name 'sample-boot-app-*.jar' ! -name '*-original.jar' 2>/dev/null | head -1)"
  [[ -n "$jar" && -f "$jar" ]] || die "未找到 sample JAR，请先执行: ./scripts/build.sh"
  echo "$jar"
}

stop_sample_on_port() {
  local port="${1:-${SMOKE_PORT:-18080}}"
  if command -v lsof >/dev/null 2>&1; then
    local pids
    pids="$(lsof -ti ":${port}" 2>/dev/null || true)"
    if [[ -n "${pids}" ]]; then
      log_warn "释放端口 ${port} 上的进程: ${pids}"
      # shellcheck disable=SC2086
      kill ${pids} 2>/dev/null || true
      sleep 1
    fi
  fi
}

wait_for_health() {
  local base_url="${1:-$DEFAULT_BASE_URL}"
  local attempts="${2:-90}"
  log_info "等待应用就绪: ${base_url}/actuator/health (最多 ${attempts}s)"
  local i=1
  while [[ "$i" -le "$attempts" ]]; do
    if curl -sf "${base_url}/actuator/health" >/dev/null 2>&1; then
      log_info "应用已就绪"
      return 0
    fi
    sleep 1
    i=$((i + 1))
  done
  die "应用启动超时"
}

json_field() {
  local json="$1"
  local field="$2"
  if command -v jq >/dev/null 2>&1; then
    echo "$json" | jq -r ".${field} // empty"
  else
    echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('${field}','') if isinstance(d,dict) else '')"
  fi
}

# 读取 JSON 数组元素字段，如 json_array_field "$body" items 0 label
json_array_field() {
  local json="$1"
  local array_name="$2"
  local index="$3"
  local field="$4"
  if command -v jq >/dev/null 2>&1; then
    echo "$json" | jq -r ".${array_name}[${index}].${field} // empty"
  else
    echo "$json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
arr = d.get('${array_name}', [])
idx = int('${index}')
field = '${field}'
print(arr[idx].get(field, '') if isinstance(arr, list) and len(arr) > idx else '')
"
  fi
}

assert_http_status() {
  local expected="$1"
  local actual="$2"
  local hint="${3:-}"
  [[ "$actual" == "$expected" ]] || die "HTTP 状态期望 ${expected} 实际 ${actual}${hint:+ ($hint)}"
}

assert_eq() {
  local expected="$1"
  local actual="$2"
  local label="${3:-value}"
  [[ "$actual" == "$expected" ]] || die "${label} 期望 [${expected}] 实际 [${actual}]"
}

assert_contains() {
  local haystack="$1"
  local needle="$2"
  local label="${3:-output}"
  [[ "$haystack" == *"$needle"* ]] || die "${label} 未包含 [${needle}]"
}

curl_json() {
  local method="$1"
  local url="$2"
  shift 2
  curl -sS -X "$method" "$url" "$@"
}

# 格式化 JSON 输出到终端（失败则原样打印）
pretty_json() {
  local json="$1"
  if command -v jq >/dev/null 2>&1; then
    echo "$json" | jq '.' 2>/dev/null || echo "$json"
  else
    echo "$json" | python3 -m json.tool 2>/dev/null || echo "$json"
  fi
}

# 打印接口请求摘要
log_api_request() {
  local method="$1"
  local path="$2"
  local req_body="${3:-}"
  if [[ -n "$req_body" ]]; then
    log_info "请求: ${method} ${path}"
    log_info "请求体:"
    pretty_json "$req_body" | sed 's/^/  /'
  else
    log_info "请求: ${method} ${path}"
  fi
}

# 打印 HTTP 响应；mask_token=1 时对 accessToken 脱敏展示
log_api_response() {
  local label="$1"
  local status="$2"
  local body="$3"
  local mask_token="${4:-0}"
  log_info "${label} HTTP ${status}"
  log_info "响应体:"
  if [[ "$mask_token" == "1" ]]; then
    echo "$body" | python3 -c "
import json, sys
raw = sys.stdin.read()
try:
    d = json.loads(raw)
except json.JSONDecodeError:
    print(raw)
    sys.exit(0)
t = d.get('accessToken')
if isinstance(t, str) and t:
    d['accessToken'] = t[:16] + '...(' + str(len(t)) + ' chars)'
print(json.dumps(d, ensure_ascii=False, indent=2))
" 2>/dev/null | sed 's/^/  /' || pretty_json "$body" | sed 's/^/  /'
  else
    pretty_json "$body" | sed 's/^/  /'
  fi
}

# 从 curl -D 响应头中读取指定 header（不区分大小写）
response_header_value() {
  local headers="$1"
  local name="$2"
  echo "$headers" | awk -v name="$name" '
    BEGIN { IGNORECASE = 1 }
    $1 ~ "^" name ":" { sub(/^[^:]+:[ ]*/, ""); gsub(/\r$/, ""); print; exit }
  '
}
