#!/usr/bin/env bash
# 冒烟用例（假定 sample 已启动）。由 test-sample.sh / smoke-test.sh 调用。

run_smoke_cases() {
  local body headers status token
  local base_url="${SMOKE_BASE_URL:?SMOKE_BASE_URL required}"
  local trace_id="${SMOKE_TRACE_ID:-${DEFAULT_TRACE_ID}}"
  local smoke_log="${SMOKE_LOG:-}"

  log_info "=== 1. ping + X-Trace-Id 透传 ==="
  headers="$(curl -sS -D - -o /tmp/smoke-ping-body.json \
    -H "X-Trace-Id: ${trace_id}" "${base_url}/api/sample/ping")"
  status="$(echo "$headers" | head -1 | awk '{print $2}')"
  assert_http_status "200" "$status" "ping"
  assert_contains "$headers" "X-Trace-Id: ${trace_id}" "响应头"
  body="$(cat /tmp/smoke-ping-body.json)"
  assert_eq "ok" "$(json_field "$body" status)" "ping.status"

  log_info "=== 2. 无 X-Trace-Id：服务自建 TraceId（本地/测试场景）==="
  headers="$(curl -sS -D - -o /tmp/smoke-ping-auto.json "${base_url}/api/sample/ping")"
  status="$(echo "$headers" | head -1 | awk '{print $2}')"
  assert_http_status "200" "$status" "ping without trace header"
  local auto_trace auto_trace_2
  auto_trace="$(response_header_value "$headers" "X-Trace-Id")"
  [[ -n "${auto_trace}" ]] || die "无头请求应回写 X-Trace-Id 响应头"
  [[ "${#auto_trace}" -ge 16 ]] || die "自建 traceId 长度异常: ${auto_trace}"
  [[ "${auto_trace}" != "${trace_id}" ]] || die "自建 traceId 不应与脚本固定网关 ID 相同 (${trace_id})"
  headers="$(curl -sS -D - -o /tmp/smoke-ping-auto-2.json "${base_url}/api/sample/ping")"
  auto_trace_2="$(response_header_value "$headers" "X-Trace-Id")"
  [[ -n "${auto_trace_2}" ]] || die "第二次无头请求也应回写 X-Trace-Id"
  [[ "${auto_trace}" != "${auto_trace_2}" ]] || die "无头请求每次应生成新 traceId（${auto_trace}）"
  log_info "自建 traceId 示例: ${auto_trace} / ${auto_trace_2}"

  log_info "=== 3. 登录获取 JWT ==="
  body="$(curl_json GET "${base_url}/api/sample/auth/login" -H "X-Trace-Id: ${trace_id}")"
  token="$(json_field "$body" token)"
  [[ -n "$token" ]] || die "login 未返回 token: ${body}"
  log_info "token 已获取（长度 ${#token}）"

  log_info "=== 4. 受保护接口（带 Token）==="
  body="$(curl -sS -w '\n%{http_code}' \
    -H "X-Trace-Id: ${trace_id}" \
    -H "Authorization: Bearer ${token}" \
    "${base_url}/api/sample/secure/hello")"
  status="${body##*$'\n'}"
  body="${body%$'\n'*}"
  assert_http_status "200" "$status" "secure/hello"
  assert_eq "secure-ok" "$(json_field "$body" message)" "secure message"

  log_info "=== 5. 未认证 401 + traceId ==="
  body="$(curl -sS -w '\n%{http_code}' \
    -H "X-Trace-Id: ${trace_id}" \
    "${base_url}/api/sample/secure/hello")"
  status="${body##*$'\n'}"
  body="${body%$'\n'*}"
  assert_http_status "401" "$status" "secure without token"
  assert_eq "${trace_id}" "$(json_field "$body" traceId)" "401 traceId"
  assert_eq "UNAUTHORIZED" "$(json_field "$body" code)" "401 code"

  log_info "=== 6. 业务异常 500 + traceId ==="
  body="$(curl -sS -w '\n%{http_code}' \
    -H "X-Trace-Id: ${trace_id}" \
    "${base_url}/api/sample/error/runtime")"
  status="${body##*$'\n'}"
  body="${body%$'\n'*}"
  assert_http_status "500" "$status" "error/runtime"
  assert_eq "${trace_id}" "$(json_field "$body" traceId)" "500 traceId"
  assert_eq "INTERNAL_ERROR" "$(json_field "$body" code)" "500 code"

  log_info "=== 7. 404 + traceId（白名单路径）==="
  body="$(curl -sS -w '\n%{http_code}' \
    -H "X-Trace-Id: ${trace_id}" \
    "${base_url}/api/sample/error/not-exists")"
  status="${body##*$'\n'}"
  body="${body%$'\n'*}"
  assert_http_status "404" "$status" "not-exists"
  assert_eq "${trace_id}" "$(json_field "$body" traceId)" "404 traceId"
  assert_eq "NOT_FOUND" "$(json_field "$body" code)" "404 code"

  log_info "=== 8. 操作日志接口 POST /orders ==="
  body="$(curl -sS -w '\n%{http_code}' -X POST \
    -H "X-Trace-Id: ${trace_id}" \
    -H "Authorization: Bearer ${token}" \
    "${base_url}/api/sample/orders")"
  status="${body##*$'\n'}"
  body="${body%$'\n'*}"
  assert_http_status "200" "$status" "POST orders"
  assert_eq "created" "$(json_field "$body" status)" "order status"

  if [[ "${SMOKE_LOG_CHECK:-0}" == "1" && -n "${smoke_log}" ]]; then
    log_info "=== 9. 校验请求摘要日志（SLF4J DEBUG）==="
    sleep 2
    if [[ -f "${smoke_log}" && -s "${smoke_log}" ]]; then
      local log_content
      log_content="$(cat "${smoke_log}")"
      # RequestLoggingFilter: traceId=xxx ；Logback: [traceId=xxx]
      assert_contains "${log_content}" "${trace_id}" "smoke log traceId"
      assert_contains "${log_content}" "userId=1" "smoke log userId after auth"
      log_info "请求日志片段见: ${smoke_log}"
    else
      log_warn "后台日志文件为空或未落盘，已跳过文件校验（HTTP 用例已通过）"
    fi
  else
    log_info "=== 9. 跳过日志文件校验（使用 test-sample 时请查看 run-sample 终端）==="
  fi

  log_info "全部冒烟用例通过"
}
