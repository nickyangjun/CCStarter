# common-log-autoconfigure

链路 TraceId（MDC `tid`）、请求 SLF4J 摘要、操作日志切面（**可发布** SNAPSHOT）。

| 项 | 值 |
|----|-----|
| 配置前缀 | `component.log` |
| MDC 约定 | [docs/architecture/logging.md](../../docs/architecture/logging.md) |
| 业务接入 | [docs/features/log/integration.md](../../docs/features/log/integration.md) |

## 能力摘要

- `TraceIdFilter`：透传 `X-Trace-Id`，无头可自建；**禁止**有头时重新生成
- `MdcUserContextFilter`：独立 `userId` / `username`（optional Security）
- `RequestLoggingFilter`：仅 SLF4J，不落库
- `@OperationLog` + `OperationLogRecorder` SPI

与 **common-exception** 协作：错误 JSON 字段 `traceId`（读 MDC `tid`）。

业务项目请使用 **`common-log-spring-boot-starter`**。
