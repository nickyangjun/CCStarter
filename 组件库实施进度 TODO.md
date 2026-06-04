# 组件库实施进度 TODO

> **最后更新**：2026-06-04 | **当前阶段**：P1 + P2 可发布；**P3 common-log 阶段 0 ✅**

---

## 二、总体进度一览

| 阶段 | 范围 | 状态 |
|------|------|------|
| P0 | 工程骨架 | ✅ 已完成 |
| P1 | common-exception | ✅ **可发布**（1.0.0-SNAPSHOT） |
| **P2** | common-auth | ✅ **可发布**（1.0.0-SNAPSHOT） |
| **P3** | common-log | 🟡 阶段 0 ✅，待编码 |
| P4～P6 | file / dict / sms | ⬜ 未开始 |

---

## 三、P1：common-exception（可发布）

### 发布清单

- [x] 功能：统一异常 JSON、条件装配、SPI
- [x] 单测 + sample `ExceptionIntegrationTest`
- [x] CI：`.github/workflows/ci.yml`（`mvn verify`）
- [x] BOM / parent 已登记模块坐标
- [x] 文档：`docs/features/exception/`、指南 §5.6、README 接入说明
- [ ] 私服 RELEASE 发布（按团队 Maven 流程执行 `deploy`）
- [ ] 真实业务项目首次接入回归（可选）

**发布坐标**：`com.company.component:common-exception-spring-boot-starter:1.0.0-SNAPSHOT`（import `company-component-bom`）

---

## 四、P2：common-auth

### 4.0 阶段 0

- [x] `docs/features/auth/README.md`、`design.md`、`phase0-checklist.md`
- [x] 附录 A 登记（阶段 0 进行中 → 设计已完成）

### 4.1 模块骨架

- [x] `common-auth-autoconfigure`、`common-auth-spring-boot-starter` POM
- [x] 父工程 modules、BOM（含 jjwt 0.12.6）
- [x] `AuthProperties` + `AuthAutoConfiguration` / `AuthSecurityAutoConfiguration`
- [x] `JwtService`、`JwtAuthenticationFilter`、`SecurityFilterChain`（白名单用 `AntPathRequestMatcher`）
- [x] 单测 `AuthAutoConfigurationTests`、`JwtServiceTest` + sample `AuthIntegrationTest`
- [x] 与 exception 401/403 JSON 联调（`ApiErrorResponseHttpWriter` + 映射扩展）

### 4.2 发布清单（可发布）

- [x] 功能：JWT 签发/校验、无状态 Security 链、白名单、条件装配、SPI
- [x] 与 exception 401/403 统一 JSON
- [x] 单测 + sample `AuthIntegrationTest`
- [x] CI：`mvn verify`（含 auth 模块）
- [x] BOM / parent 已登记；文档与根 README 接入说明
- [x] git commit + push（`8ff50de` / `e4d0e5e`）
- [ ] 私服 RELEASE 发布（按团队 Maven 流程执行 `deploy`）
- [ ] 真实业务项目首次接入回归（可选）

**发布坐标**：`com.company.component:common-auth-spring-boot-starter:1.0.0-SNAPSHOT`（**建议与 exception starter 一并引入**）

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-auth | ✅ | ✅ | ✅ | ✅ SNAPSHOT |

---

## 五、P3：common-log

### 5.0 阶段 0

- [x] `docs/features/log/README.md`、`design.md`、`phase0-checklist.md`
- [x] `docs/architecture/logging.md`（TraceId / MDC `tid` / SkyWalking）
- [x] 团队决策：网关 `X-Trace-Id` 透传、请求日志仅 SLF4J、操作日志 SPI 异步、错误体必带 traceId
- [x] TraceId 仅链路追踪（无业务语义）；`tid` 与 `userId` 分 MDC、日志分列打印

### 5.1 待实现（按 design 分期）

- [ ] Maven：`common-log-autoconfigure`、`common-log-spring-boot-starter`
- [ ] `TraceIdFilter` + MDC `tid`（禁止有头时重建）
- [ ] `RequestLoggingFilter`（仅 SLF4J）
- [ ] **exception** 协作：`ApiErrorResponse.traceId`（读 MDC）
- [ ] `@OperationLog` + `OperationLogRecorder` SPI
- [ ] sample 集成 + `mvn verify`

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-log | ✅ | ⬜ | ⬜ | ⬜ |

---

## 七、进度日志

| 日期 | 动作 | 结果 |
|------|------|------|
| 2026-06-04 | P1 提交 + CI | 可发布 SNAPSHOT |
| 2026-06-04 | P2 auth 阶段 0 + POM 骨架 | 待 Phase 3 编码 |
| 2026-06-04 | P2 Phase 3 JWT + Security + 全量 verify | 本地构建通过 |
| 2026-06-04 | P2 标可发布 + 业务接入文档 | SNAPSHOT 可引用 |
| 2026-06-04 | P3 log 阶段 0 + logging.md | 五项决策固化 |

---

## 八、快速链接

- [log 设计](./docs/features/log/design.md)
- [MDC 约定](./docs/architecture/logging.md)
- [auth 设计](./docs/features/auth/design.md)
- [exception 模块](./docs/features/exception/)
