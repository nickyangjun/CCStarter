# common-log · 业务接入指南

> 一期 SNAPSHOT 可发布。推荐与 **common-exception**、**common-auth** 一并引入：错误体带 `traceId`，鉴权后 `userId` 写入独立 MDC。

---

## 1. Maven 依赖

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.company.component</groupId>
            <artifactId>company-component-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-exception-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-log-spring-boot-starter</artifactId>
    </dependency>
    <!-- 若需 JWT，再加 common-auth-spring-boot-starter -->
</dependencies>
```

本地开发：`mvn clean install`（组件库根目录）。

---

## 2. 网关与 TraceId 约定

| 规则 | 说明 |
|------|------|
| 权威来源 | 网关生成 `X-Trace-Id`，**全链路透传**，服务**禁止**在有网关头时重新生成 |
| MDC 键 | **`tid`**（SkyWalking 对齐，Logback 用 `%X{tid}`） |
| 与用户信息 | **禁止**把 userId 拼进 TraceId；`userId` / `username` 为**独立** MDC 键 |
| 无网关场景 | 测试/内部调用可 `trace.allow-local-generate=true` 自建 UUID |

详见 [logging.md](../../architecture/logging.md)。

---

## 3. 配置

### 3.1 最小配置

```yaml
component:
  exception:
    enabled: true
    include-trace-id: true    # 4xx/5xx 响应带 traceId（默认 true）
  log:
    enabled: true
    trace:
      header-names:
        - X-Trace-Id
      allow-local-generate: true
      response-header: true
    request:
      enabled: true
      log-level: DEBUG          # 生产建议 DEBUG 或 INFO + sample-rate
      exclude-paths:
        - /actuator/**
```

### 3.2 Logback 版式（推荐）

```xml
<Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [traceId=%X{tid}] [userId=%X{userId}] %-5level %logger{36} - %msg%n</Pattern>
```

输出示例：

```text
[2025-01-01 10:00:00.123] [traceId=abc123] [userId=10086] INFO  c.c.order.OrderController - 接口调用成功
```

### 3.3 配置项速查

| 配置键 | 默认 | 说明 |
|--------|------|------|
| `component.log.enabled` | `false` | 总开关 |
| `trace.header-names` | `X-Trace-Id` | 按顺序取第一个非空 |
| `trace.allow-local-generate` | `true` | 无上游头时是否自建 |
| `trace.response-header` | `true` | 是否回写 `X-Trace-Id` |
| `request.enabled` | `true` | 请求摘要 SLF4J（**不落库**） |
| `request.log-level` | `DEBUG` | `DEBUG` / `INFO` 等 |
| `request.sample-rate` | `1.0` | `INFO` 时采样率 |
| `request.exclude-paths` | `/actuator/**` | 不打摘要的路径 |
| `operation.enabled` | `true` | 有 SPI Bean 时启用切面 |

---

## 4. 错误响应中的 traceId

启用 log 且请求经过 `TraceIdFilter` 后，所有 **4xx/5xx** JSON 均含 `traceId`（与 MDC `tid` 一致）：

```json
{
  "code": "INTERNAL_ERROR",
  "message": "服务器内部错误",
  "traceId": "gateway-trace-001",
  "timestamp": "2026-06-04T10:00:00+08:00",
  "path": "/api/demo"
}
```

用户报障时提供 `traceId` 或响应头 `X-Trace-Id`，在日志平台按 `[traceId=...]` / `%X{tid}` 检索。

---

## 5. 业务操作日志（SPI）

### 5.1 实现 SPI（必须异步落库）

```java
@Bean
public OperationLogRecorder operationLogRecorder() {
    return entry -> CompletableFuture.runAsync(() -> {
        // 写 DB / ES / MQ
    });
}
```

组件**不强制**线程模型，但**必须**快速返回，不得阻塞主链路。

### 5.2 标注接口

```java
@OperationLog(module = "order", action = "create")
@PostMapping
public OrderDto create(@RequestBody CreateOrderRequest req) {
    // ...
}
```

`OperationLogEntry` 自动带上 MDC 中的 `traceId`（tid）、`operatorId`（userId）、`operatorName`（username）等。

无 `OperationLogRecorder` Bean 时，**不注册**操作日志切面，应用正常启动。

---

## 6. 与 common-auth 协作

- 引入 auth 且 JWT 含 `userId` 时，认证成功后 MDC 写入 **`userId`**（来自 `Authentication#getDetails()`）与 **`username`**。
- TraceId 仍仅来自 `X-Trace-Id` 或自建，与 userId **无关**。

---

## 7. 验证清单

- [ ] 网关对外请求均带 `X-Trace-Id`
- [ ] 服务日志 `%X{tid}` 与网关 ID 一致
- [ ] 4xx/5xx 响应含 `traceId`
- [ ] 未将 userId 拼入 TraceId
- [ ] 操作日志 SPI 异步落库
- [ ] `mvn clean verify` 通过

---

## 相关文档

- [能力概述](./README.md)
- [详细设计](./design.md)
- [MDC 全库约定](../../architecture/logging.md)
- [样例应用](../../../company-component-samples/sample-boot-app/)
