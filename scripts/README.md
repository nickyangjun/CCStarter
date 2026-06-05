# 脚本说明

职责拆分：**启动** 与 **测试** 分开；CI / 提交前用一键脚本。

| 脚本 | 职责 |
|------|------|
| [build.sh](./build.sh) | 编译 + `mvn verify` + 打 sample JAR |
| [run-sample.sh](./run-sample.sh) | **仅启动** sample（前台，看日志） |
| [test-sample.sh](./test-sample.sh) | **仅测试**（要求 sample 已启动） |
| [smoke-test.sh](./smoke-test.sh) | 一键：build → 后台启动 → test → 停进程 |
| [deploy-sample-docker.sh](./deploy-sample-docker.sh) | Docker 部署 + `test-sample.sh` |

## 本地开发（推荐）

```bash
chmod +x scripts/*.sh scripts/lib/*.sh

# 终端 1：启动
./scripts/run-sample.sh

# 终端 2：测试（可反复执行）
./scripts/test-sample.sh
```

## 提交前 / CI

```bash
./scripts/smoke-test.sh
```

等价于 `build.sh` + 后台启动 + `test-sample.sh` + 自动停止。

`run-sample.sh` / `smoke-test.sh` 默认 `--spring.profiles.active=test`，加载 `application-test.yml` 中的登录测试验证码（固定码 `123456`）。

## 环境变量

| 变量 | 默认 | 说明 |
|------|------|------|
| `SMOKE_PORT` | `18080` | sample 端口 |
| `SMOKE_BASE_URL` | `http://127.0.0.1:18080` | 测试基地址 |
| `SMOKE_TRACE_ID` | `smoke-trace-0001` | 模拟网关 TraceId |
| `BUILD_FIRST` | `1` | `run-sample` / `smoke-test` 是否先 build |
| `SMOKE_LOG` | `.smoke/...` | 仅 `smoke-test` 后台日志；`test-sample` 默认不校验文件 |

## 测试覆盖（test-sample / smoke-test）

1. **网关头透传**：脚本固定 `SMOKE_TRACE_ID`（默认 `smoke-trace-0001`），模拟网关  
2. **无网关头自建**：不带 `X-Trace-Id`，断言响应头回写且每次请求生成**新** UUID  
3. 短信登录 JWT（打印请求/响应；`accessToken` 脱敏）  
4. 受保护接口 200  
5. 无 Token → 401 + `traceId`  
6. 500 / 404 + `traceId`  
7. `POST /orders`  
8. `GET /api/dict/gender`（白名单，无 Token）  
9. 后台启动时校验日志文件（仅 `smoke-test.sh`）  

自定义网关 TraceId：`SMOKE_TRACE_ID=my-gateway-id ./scripts/test-sample.sh`

依赖：`curl`、`python3`；可选 `jq`。

## 登录用例失败（401 UNAUTHORIZED）

若第 3 步返回 `code=UNAUTHORIZED`、`path=/api/auth/sms/login`，表示**登录 URL 未匿名放行**（常见原因：**18080 上仍是旧 sample 进程**）。

```bash
# 停掉旧进程后重新构建并启动
./scripts/build.sh
./scripts/run-sample.sh
# 另开终端
./scripts/test-sample.sh
```
