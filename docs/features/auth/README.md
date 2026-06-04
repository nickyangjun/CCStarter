# common-auth · 能力概述

| 项 | 内容 |
|----|------|
| 配置前缀 | `component.auth` |
| Maven starter | `common-auth-spring-boot-starter` |
| 设计文档 | [design.md](./design.md) |
| **业务接入** | **[integration.md](./integration.md)** |
| 阶段 0 检查 | [phase0-checklist.md](./phase0-checklist.md) |
| 发布状态 | **可发布**（1.0.0-SNAPSHOT，需与 exception 一并引入） |

---

## 一期目标（做）

- **JWT** 签发与校验工具（基于 jjwt，版本由 BOM 管理）。
- **Servlet 环境**下注册 `SecurityFilterChain`：无状态会话、JWT 过滤器、白名单路径。
- 配置项：`enabled`、JWT 密钥、过期时间、匿名访问路径（whitelist）。
- `enabled=false` 时：**不注册**本组件的 Security 相关 Bean，避免与「仅引依赖」的默认 Security 行为纠缠（见 design.md）。
- 与 `common-exception` 协作：鉴权失败/访问拒绝的 HTTP 响应走统一错误体（由 exception 模块输出 JSON）。
- 预留 SPI：`AuthUserDetailsLoader`（业务提供用户加载）、可选 `JwtClaimsCustomizer`。

## 一期不做

- 完整 OAuth2/OIDC 授权服务器、SSO 中心。
- 网关层鉴权（API Gateway 另议；服务内与本组件分层）。
- 细粒度业务权限模型（角色/数据权限表在业务系统）。
- Reactive Web（WebFlux）安全链（一期仅 `SERVLET`）。
- 登录页、验证码、短信登录 UI。

---

## 配置项速查（一期规划）

| 配置键 | 类型 | 默认 | 敏感 | 说明 |
|--------|------|------|------|------|
| `enabled` | boolean | `false` | 否 | 总开关 |
| `jwt-secret` | string | 无 | **是** | `enabled=true` 时必填 |
| `expire-minutes` | int | `120` | 否 | Access Token 过期分钟 |
| `whitelist` | string[] | 见 design | 否 | 匿名路径 Ant 风格 |
| `header-name` | string | `Authorization` | 否 | Token 请求头 |
| `token-prefix` | string | `Bearer ` | 否 | 头前缀 |

---

## 多环境（测试 / 正式）

| 环境 | 要求 |
|------|------|
| `test` / `prod` | `jwt-secret` 必须由环境变量或配置中心注入，如 `${JWT_SECRET}` |
| 所有环境 | `expose` 类配置遵循建设指南 §5.6；**禁止**在 prod yml 写死密钥 |

---

## 业务接入（摘要）

1. 依赖：`common-exception-spring-boot-starter` + `common-auth-spring-boot-starter`（import BOM）。
2. 配置：`component.auth.enabled=true`，`jwt-secret` 用 `${JWT_SECRET}` 注入。
3. 登录：业务 Controller 注入 `JwtService#createToken`，组件不提供登录页。
4. 白名单：登录、健康检查、静态公开 API 等写入 `whitelist`。

完整步骤见 **[integration.md](./integration.md)**。

---

## 相关文档

- [建设指南 · P2](../../../Spring Boot 可插拔积木组件建设指南.md)
- [exception 模块](../exception/README.md)（错误响应格式）
