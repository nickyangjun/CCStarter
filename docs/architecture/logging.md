# 日志与 MDC 约定（全库标准）

> **团队锁定**（2026-06-04）。所有组件、业务服务、日志配置必须遵守；与 [SkyWalking Logback MDC](https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/application-toolkit-logback-1.x/) 对齐，避免接入 APM 后全量返工。

---

## 1. TraceId 权威来源

| 场景 | 规则 |
|------|------|
| 经网关的 HTTP 请求 | **必须以网关 `X-Trace-Id` 为准**；服务只透传，**禁止**重新生成 |
| 存在任意上游 TraceId | 请求头 `X-Trace-Id` 非空 → 原样写入 MDC，并向下游/响应透传 |
| 未经网关 | 内部调用、本地测试、单测等 → 允许服务**自建** TraceId |
| 生成算法 | UUID 或团队统一算法（在 `component.log` 配置中声明，禁止硬编码在业务代码） |

**禁止**：网关已带 `X-Trace-Id` 时，因「未接入 SkyWalking」等原因另起一个新 ID。

---

## 1.1 TraceId 唯一职责：链路追踪

TraceId **只**用于把**同一次请求**上的所有日志串成一条链路，除此以外不承担任何业务含义。

| 要求 | 说明 |
|------|------|
| **短、唯一** | 能在日志/APM 中唯一定位一次请求 |
| **无意义** | 纯技术 ID，不编码业务语义 |
| **无业务属性** | **禁止**嵌入 userId、orderId、tenantId、手机号等 |
| **格式** | 网关或自建：`UUID` / 随机串 / Snowflake 等；由 `component.log.trace.id-generator` 配置，**禁止**在业务代码拼业务字段 |

**禁止示例**（不得作为 TraceId 或 `X-Trace-Id`）：

- `10086-abc123`（前缀用户 ID）
- `order:9527:uuid`（含订单语义）
- `tenant1_user10086_20250101`（可读业务串）

用户信息、租户、订单等 **必须**使用**独立 MDC 键**（如 `userId`、`username`），与 TraceId **分键存储、分键打印**，**禁止**合并成一个值。

---

## 2. HTTP 头与 JSON 字段

| 名称 | 用途 |
|------|------|
| 请求头 `X-Trace-Id` | 网关生成；全链路透传 |
| 响应头 `X-Trace-Id`（可选） | `component.log.trace.response-header=true` 时回写，便于客户端报障 |
| JSON 字段 `traceId` | **所有 4xx/5xx** 统一错误体（`common-exception`）必填，值与 MDC `tid` 一致 |

---

## 3. MDC 键名（SkyWalking 对齐）

| MDC Key | 设置方 | 说明 |
|---------|--------|------|
| **`tid`** | **common-log**（来自 `X-Trace-Id` 或自建） | SkyWalking Logback 标准键，`%X{tid}`；与 `TraceContext.traceId()` 语义一致 |
| `segmentId` | SkyWalking Agent | 组件**不写入**；Agent 接入后自动出现 |
| `spanId` | SkyWalking Agent | 组件**不写入** |
| `userId` | common-log（协作 auth） | 当前登录用户 ID；**与 tid 无关**，禁止写入 tid |
| `username` | common-log（协作 auth） | JWT `sub` 等；**与 tid 无关** |

**约定**：

- **MDC 键** `tid` 仅存 TraceId（SkyWalking 对齐）；**禁止**把 `userId` 拼进 `tid`。
- **日志 Pattern** 可同时打印多个 MDC 键，键名在版式里可读即可，例如 `[traceId=%X{tid}] [userId=%X{userId}]`（`traceId=` 仅为日志展示标签，**不是** MDC 键名）。
- API JSON 字段名 **`traceId`**（对外契约）；值 **仅**来自 MDC `tid`，与 `userId` 字段分离。

---

## 4. 日志分类与落点

| 类型 | 含义 | 落点 | 阻塞主链路 |
|------|------|------|------------|
| **请求日志** | 访问摘要（方法、URI、状态、耗时） | 仅 **SLF4J**（文件/stdout） | 否（DEBUG/采样） |
| **技术日志** | 各模块 `Logger` 调试/错误 | SLF4J | 否 |
| **操作日志** | 登录、下单、审核等业务行为 | **SPI 落库**（DB/ES/MQ） | **必须异步**（SPI 实现责任） |

---

## 5. 模块依赖关系

| 规则 | 说明 |
|------|------|
| 业务应用 | 推荐 `common-exception` + `common-auth` + **`common-log-starter`** 统一引入 |
| 组件库内部 | **禁止** exception/auth 等 compile 依赖 log-autoconfigure |
| 协作方式 | SLF4J + 本 MDC 约定；exception 从 MDC 读 `tid` 写入 `ApiErrorResponse.traceId` |

---

## 6. Logback 配置示例（业务项目）

推荐版式（TraceId 与用户信息**分列**，不合并）：

```xml
<Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [traceId=%X{tid}] [userId=%X{userId}] %-5level %logger{36} - %msg%n</Pattern>
```

输出示例：

```text
[2025-01-01 10:00:00.123] [traceId=abc123] [userId=10086] INFO  c.c.order.OrderController - 接口调用成功
```

- 未登录时 `userId` 可为空，**仍保留** `[traceId=...]`。
- 接入 SkyWalking Agent 后，`%X{tid}` 与网关 `X-Trace-Id` / APM 一致；无 Agent 时由 common-log 写入 `tid`。

---

## 7. 变更流程

- 新增 MDC 键 → 先更新本文件，再改 common-log / 业务配置。
- 修改 TraceId 规则 → 评审 + CHANGELOG + 通知网关与 APM 负责人。
