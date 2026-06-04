# common-log · 能力概述

| 项 | 内容 |
|----|------|
| 配置前缀 | `component.log` |
| Maven starter | `common-log-spring-boot-starter` |
| 设计文档 | [design.md](./design.md) |
| MDC 全库约定 | [logging.md](../../architecture/logging.md) |
| **业务接入** | **[integration.md](./integration.md)** |
| 阶段 0 检查 | [phase0-checklist.md](./phase0-checklist.md) |
| 发布状态 | **可发布**（1.0.0-SNAPSHOT，建议与 exception 一并引入） |

---

## 一期目标（做）

### A. 请求链路（技术日志）

- 从请求头读取 **`X-Trace-Id`**（网关权威）；有则透传，**无则**允许自建（内部/测试）。
- 将 TraceId 写入 MDC 键 **`tid`**（SkyWalking 标准，见 [logging.md](../../architecture/logging.md)）。
- 可选回写响应头 `X-Trace-Id`。
- **请求访问摘要**仅输出到 **SLF4J**（可配置级别、采样、路径排除），**不落库**。
- 与 **common-auth** 协作：将 `userId` / `username` 写入**独立** MDC 键（optional Security）；**禁止**与 TraceId 合并。
- TraceId **仅**链路追踪：短、唯一、无业务语义（UUID/随机/Snowflake，见 [logging.md §1.1](../../architecture/logging.md)）。

### B. 业务操作日志

- `@OperationLog` + AOP 切面，组装 `OperationLogEntry`。
- SPI **`OperationLogRecorder`**：业务落库（DB/ES/MQ）；无 Bean 则不注册切面，**不导致启动失败**。
- 文档**强烈推荐** SPI 实现**异步**落库，组件层不强制线程模型。

### C. 与 common-exception 协作

- 所有 **4xx/5xx** 的 `ApiErrorResponse` **必须**包含字段 **`traceId`**（值 = MDC `tid`）。
- 由 exception 模块读取 MDC（不 compile 依赖 log）。

## 一期不做

- 请求访问日志落库、ES 索引维护。
- 替代 SkyWalking / Zipkin / 网关 Access Log。
- 业务表结构、订单/审批等领域字段（走 SPI）。
- Reactive WebFlux 链路（一期仅 `SERVLET`）。
- 组件库内部模块强制依赖 log-autoconfigure。

---

## 团队锁定决策（2026-06-04）

| # | 议题 | 结论 |
|---|------|------|
| 1 | TraceId 以谁为准 | **网关 `X-Trace-Id`**；服务只透传；无网关头时才自建 |
| 2 | 请求日志落哪里 | **仅 SLF4J**；落库仅操作日志 SPI |
| 3 | 操作日志异步 | **必须不阻塞主链路**；SPI 实现**强烈推荐**异步 |
| 4 | 错误响应 traceId | **必须带**；与 exception 统一 |
| 5 | SkyWalking | 未来必接；MDC 现用 **`tid`** 对齐 SW |
| 6 | 全模块依赖 log | **否**；业务引 starter，库内 SLF4J + 约定 |
| 7 | TraceId 与用户信息 | **分键**：`tid` 仅 TraceId；`userId` 等独立 MDC；日志 `[traceId=…] [userId=…]` 分列，**禁止**拼成一个值 |

---

## 配置项速查

| 配置键 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `enabled` | boolean | `false` | 总开关 |
| `trace.header-names` | string[] | `X-Trace-Id` | 按顺序取第一个非空头 |
| `trace.allow-local-generate` | boolean | `true` | 无上游头时是否自建 |
| `trace.response-header` | boolean | `true` | 是否回写 `X-Trace-Id` |
| `request.enabled` | boolean | `true` | 请求摘要 SLF4J |
| `request.log-level` | string | `DEBUG` | 避免生产 INFO 刷屏 |
| `request.sample-rate` | double | `1.0` | INFO 采样时用 |
| `operation.enabled` | boolean | `true` | 切面开关（依赖 SPI 才真正落库） |
| `mask.keys` | string[] | 见 design | 脱敏字段名 |

完整说明见 [design.md](./design.md)。

---

## 业务接入（摘要）

1. 引入 `common-log-spring-boot-starter`（与 exception 一并引入；有 JWT 时加 auth）。
2. 网关保证每条外部请求带 **`X-Trace-Id`**（服务只透传）。
3. `component.log.enabled=true`；Logback：`[traceId=%X{tid}] [userId=%X{userId}]`。
4. 操作日志：实现 `OperationLogRecorder`，**异步**落库。
5. 排错：响应体 / 头中的 `traceId` 检索日志。

完整步骤见 **[integration.md](./integration.md)**。

**发布坐标**：`com.company.component:common-log-spring-boot-starter:1.0.0-SNAPSHOT`

---

## 相关文档

- [建设指南 · P3](../../../Spring Boot 可插拔积木组件建设指南.md)
- [exception 模块](../exception/README.md)
- [auth 模块](../auth/README.md)
