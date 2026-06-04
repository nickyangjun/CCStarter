#!/usr/bin/env bash
# 全量编译 + 单测 + 打包 sample 可运行 JAR

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

SKIP_VERIFY="${SKIP_VERIFY:-0}"
SKIP_PACKAGE="${SKIP_PACKAGE:-0}"

cd "${ROOT_DIR}"

if [[ "${SKIP_VERIFY}" != "1" ]]; then
  log_info "Maven verify（含全部模块单测）..."
  mvn -B clean verify
else
  log_info "跳过 verify（SKIP_VERIFY=1）"
fi

if [[ "${SKIP_PACKAGE}" != "1" ]]; then
  log_info "打包 sample-boot-app..."
  mvn -B package -pl "${SAMPLE_MODULE}" -am -DskipTests
  log_info "JAR: $(find_sample_jar)"
else
  log_info "跳过 package（SKIP_PACKAGE=1）"
fi

log_info "构建完成"
