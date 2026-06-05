#!/usr/bin/env bash
# 仅启动 sample（前台），不跑测试
#
# 测试请另开终端: ./scripts/test-sample.sh
# 一键构建+后台测+停: ./scripts/smoke-test.sh

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

BUILD_FIRST="${BUILD_FIRST:-1}"
PORT="${SMOKE_PORT:-18080}"

if [[ "${BUILD_FIRST}" == "1" ]]; then
  SKIP_VERIFY=1 "${SCRIPT_DIR}/build.sh"
fi

JAR="$(find_sample_jar)"
log_info "启动 sample: ${JAR}"
log_info "端口: ${PORT}  |  健康检查: http://127.0.0.1:${PORT}/actuator/health"
log_info "Profile: test（测试验证码见 application-test.yml）"
log_info "另开终端执行测试: ./scripts/test-sample.sh"

exec java -jar "${JAR}" \
  --spring.profiles.active=test \
  --server.port="${PORT}" \
  --logging.level.com.company.component.log.request=DEBUG
