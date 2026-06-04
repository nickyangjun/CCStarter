# common-auth-autoconfigure

JWT + Spring Security 自动配置模块（**P2 Phase 3 已实现**）。

| 项 | 值 |
|----|-----|
| 配置前缀 | `component.auth` |
| 设计文档 | [docs/features/auth/](../../docs/features/auth/) |

## 当前进度

- ✅ 阶段 0 设计文档
- ✅ `AuthProperties`（`component.auth.*`）、`JwtService`、SPI（`JwtClaimsCustomizer`、`AuthUserDetailsLoader`）
- ✅ `JwtAuthenticationFilter`、`AuthSecurityAutoConfiguration`（STATELESS + 白名单）
- ✅ 依赖 `common-exception-autoconfigure` 统一 401/403 JSON

**接入**：与 `common-exception-spring-boot-starter` 一并引入 `common-auth-spring-boot-starter`，配置 `component.auth.enabled=true` 与 `jwt-secret`（≥32 字符）。

业务项目请使用 **`common-auth-spring-boot-starter`**。
