# common-exception · 能力概述

| 项 | 内容 |
|----|------|
| 配置前缀 | `component.exception` |
| Maven starter | `common-exception-spring-boot-starter` |
| 设计文档 | [design.md](./design.md) |
| 阶段 0 检查 | [phase0-checklist.md](./phase0-checklist.md) |

---

## 一期目标（做）

- 提供统一的 **HTTP API 错误响应体**（JSON 字段约定：`code`、`message`、`timestamp`、`path` 等）。
- 通过 `@ControllerAdvice` 注册 **全局异常处理器**（仅 `enabled=true` 且 Servlet Web 环境）。
- 内置对常见异常的分层映射：`MethodArgumentNotValidException`、`BindException`、`HttpMessageNotReadableException`、`Exception` 兜底等（详见 design.md）。
- 支持可配置的 **是否返回 path**、**是否暴露堆栈**（仅 dev，生产默认关闭）。
- 预留 **错误码扩展 SPI**（`ExceptionErrorCodeResolver`），业务可注册领域错误码，**缺 SPI 不导致启动失败**。
- 与业务已有 `@ControllerAdvice`：**`@ConditionalOnMissingBean` 让路**，避免覆盖业务定制。

## 一期不做

- 不依赖具体业务表、不实现业务错误码表维护 UI。
- 不处理非 Web 环境（Reactive 一期仅文档标注，实现挂 `SERVLET` 条件）。
- 与 **common-auth** 集成后：`AuthenticationException` / `AccessDeniedException` 映射为 401/403 统一 JSON（Filter 链由 `ApiErrorResponseHttpWriter` 输出）。
- 不实现国际化消息中心（后续 MINOR 可加 `message-source` 配置）。
- 不自动修改其他组件（auth、log）的异常 JSON 格式——**以本模块为唯一 HTTP 错误体标准**。

---

## 配置项速查（一期）

| 配置键 | 类型 | 默认 | 必填 | 说明 |
|--------|------|------|------|------|
| `enabled` | boolean | `false` | 否 | 总开关 |
| `include-path` | boolean | `true` | 否 | 响应是否带请求路径 |
| `expose-stack-trace` | boolean | `false` | 否 | 是否返回堆栈（**生产必须 false**） |
| `default-error-code` | string | `INTERNAL_ERROR` | 否 | 未识别异常兜底码 |

完整说明见 [design.md](./design.md)。

---

## 多环境（测试 / 正式）

**支持**，用法与 Spring Boot 标准配置相同，组件**不**内置「测试/正式」两套代码路径。

| 环境 | 建议 |
|------|------|
| 测试 `test` | `enabled: true`，`expose-stack-trace: false` |
| 正式 `prod` | 同上；通过配置中心或环境变量覆盖，**禁止** `expose-stack-trace: true` |
| 本地 `dev` | 如需排查可临时 `expose-stack-trace: true`，勿带入 prod |

业务项目使用 `application-test.yml`、`application-prod.yml` 或配置中心分环境配置即可。详见建设指南 **§5.6** 与 sample 的 `application-*.yml` 示例。

---

## 依赖关系

- **必需（enabled 时）**：Servlet Web（`spring-boot-starter-web`）。
- **可选**：业务实现 `ExceptionErrorCodeResolver` SPI。

---

## 相关文档

- [建设指南 · P1](../../../Spring Boot 可插拔积木组件建设指南.md)
- [前缀注册表](../../architecture/config-prefix-registry.md)
