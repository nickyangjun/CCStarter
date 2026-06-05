# 组件库实施进度 TODO

> **最后更新**：2026-06-05 | **当前阶段**：P1～P3 可发布；P2.1 auth login 已发布；**P5 dict 阶段 0 已完成**

---

## 二、总体进度一览

| 阶段 | 范围 | 状态 |
|------|------|------|
| P0 | 工程骨架 | ✅ 已完成 |
| P1 | common-exception | ✅ **可发布**（1.0.0-SNAPSHOT） |
| **P2** | common-auth（JWT / Security） | ✅ **可发布**（1.0.0-SNAPSHOT） |
| **P2.1** | common-auth · login 编排 | ✅ **可发布**（**1.1.0-SNAPSHOT**；SDK/Redis 待办） |
| **P3** | common-log | ✅ **可发布**（1.0.0-SNAPSHOT） |
| P4 | common-file | ⬜ 未开始 |
| **P5** | common-dict | 🟡 **已实现**（SNAPSHOT；HTTP API 二期） |
| P6 | common-sms | ⬜ 未开始 |

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

**发布坐标**：`com.company.component:common-auth-spring-boot-starter:1.1.0-SNAPSHOT`（含 login 编排；**建议与 exception starter 一并引入**）

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-auth | ✅ | ✅ | ✅ | ✅ SNAPSHOT |
| common-auth · login | ✅ [login-design.md](docs/features/auth/login-design.md) | ✅ | ✅ | ✅ **1.1.0-SNAPSHOT** |

### 4.3 登录编排（P2.1 / auth MINOR）

- [x] 阶段 0：`login-design.md`、`login-phase0-checklist.md`、前缀 `component.auth.login`
- [x] `LoginAutoConfiguration` + `LoginProperties`（`component.auth.login`）
- [x] SMS：发码 / 登录 / 注册独立 URL；`sms-length` 4 或 6
- [x] 邮箱：发码 / 登录 / 注册独立 URL；`email-code-length` 4 或 6
- [x] 测试验码：`test.sms`（`fixed-code` 与 `mobile-suffix` 二选一）、`test.email.fixed-code`
- [x] `test.allow-in-production` 生产防呆 + 配置注释
- [x] 注册 + `login-as-register`；`LoginUserResolver` / `LoginUserRegistrar` SPI
- [x] 登录路径自动合并白名单 + sample 显式 `/api/auth/sms/**`、`/api/auth/email/**`
- [x] exception：`MappedHttpStatusException` + `LoginAuthException` 错误码映射
- [x] 占位：`StubSmsCodeSender`、`StubEmailCodeSender`、内存 `SmsCodeStore` / `EmailCodeStore`
- [x] 单测：`LoginPropertiesValidatorTest`、`SmsCodeServiceTest`、`EmailCodeServiceTest`、`LoginAutoConfigurationTests`
- [x] sample：`SampleLoginSpiConfiguration` + `AuthIntegrationTest`（短信/邮箱）
- [x] 冒烟：`POST /api/auth/sms/login`（test 固定码）；请求/响应 JSON 输出（token 脱敏）
- [x] 根 README `scripts/` 章节；`scripts/README.md` 登录 401 排查说明
- [ ] **[待办] `SmsCodeSender` 对接第三方短信 SDK**（阿里云/腾讯云等）
- [ ] **[待办] `EmailCodeSender` 对接邮件服务**（SMTP/云邮件）
- [ ] **[待办] 正式环境 `SmsCodeStore` / `EmailCodeStore` Redis 实现**
- [x] `docs/features/auth/integration.md` 增补 login 章节
- [x] auth **MINOR** 版本号与 CHANGELOG（`1.1.0-SNAPSHOT`）
- [x] test profile 配置分离（`application-test.yml`；`a4194d7`）

---

## 五、P3：common-log

### 5.0 阶段 0

- [x] `docs/features/log/README.md`、`design.md`、`phase0-checklist.md`
- [x] `docs/architecture/logging.md`（TraceId / MDC `tid` / SkyWalking）
- [x] 团队决策：网关 `X-Trace-Id` 透传、请求日志仅 SLF4J、操作日志 SPI 异步、错误体必带 traceId
- [x] TraceId 仅链路追踪（无业务语义）；`tid` 与 `userId` 分 MDC、日志分列打印

### 5.2 发布清单（可发布）

- [x] 功能：TraceId 透传、MDC、请求 SLF4J、操作日志 SPI
- [x] 与 exception `traceId` 字段、auth `userId` MDC 协作
- [x] 单测 + sample `LogIntegrationTest`
- [x] 文档：`integration.md`、`logging.md`、README 接入说明
- [x] git commit + push（与代码同 PR）
- [ ] 私服 RELEASE 发布（可选）

**发布坐标**：`com.company.component:common-log-spring-boot-starter:1.0.0-SNAPSHOT`（**建议与 exception starter 一并引入**）

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-log | ✅ | ✅ | ✅ | ✅ SNAPSHOT |

---

## 六、P5：common-dict

### 6.0 阶段 0

- [x] `docs/features/dict/README.md`、`design.md`、`phase0-checklist.md`
- [x] 附录 A 登记 `component.dict`
- [x] 统一表规范 `sys_dict_type` / `sys_dict_item`（design §4）
- [x] 团队决策：一期仅 `DictService` Bean；sample `memory` / docker `redis`

### 6.1 实施清单

- [x] `common-dict-autoconfigure`、`common-dict-spring-boot-starter` POM
- [x] 父工程 modules、BOM 登记
- [x] `DictProperties` + `DictAutoConfiguration`
- [x] SPI `DictDataProvider` + `DictService`
- [x] `InMemoryDictCache` + `RedisDictCache`（`cache.type`）
- [x] 单测 + sample `SampleDictSpiConfiguration` + `DictIntegrationTest`
- [ ] `docs/features/dict/integration.md` + CHANGELOG
- [ ] **二期**：`DictController` HTTP API + 冒烟

**发布坐标**（规划）：`com.company.component:common-dict-spring-boot-starter:1.0.0-SNAPSHOT`

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-dict | ✅ [design.md](docs/features/dict/design.md) | ✅ | ✅ | 🟡 SNAPSHOT |

---

## 七、进度日志

| 日期 | 动作 | 结果 |
|------|------|------|
| 2026-06-04 | P1 提交 + CI | 可发布 SNAPSHOT |
| 2026-06-04 | P2 auth 阶段 0 + POM 骨架 | 待 Phase 3 编码 |
| 2026-06-04 | P2 Phase 3 JWT + Security + 全量 verify | 本地构建通过 |
| 2026-06-04 | P2 标可发布 + 业务接入文档 | SNAPSHOT 可引用 |
| 2026-06-04 | P3 log 阶段 0 + logging.md | 五项决策固化 |
| 2026-06-04 | P3 log 实现 + 接入文档 | 可发布 SNAPSHOT |
| 2026-06-05 | P2.1 login 编排（SMS+邮箱+注册） | `mvn verify` 通过 |
| 2026-06-05 | 冒烟脚本登录 + 请求/响应日志输出 | `test-sample.sh` 可观测 |
| 2026-06-05 | integration 文档 + auth 1.1.0-SNAPSHOT + CHANGELOG | P2.1 文档/版本闭环 |
| 2026-06-05 | test profile 配置归位 + docker include test | `a4194d7` |
| 2026-06-05 | P5 dict 阶段 0 设计文档 | 表规范 + DictService + memory/redis |
| 2026-06-05 | P5 dict 编码（DictService + 缓存 + sample） | `mvn verify` 通过 |

---

## 八、快速链接

- [login 设计](./docs/features/auth/login-design.md)
- [log 接入](./docs/features/log/integration.md)
- [log 设计](./docs/features/log/design.md)
- [MDC 约定](./docs/architecture/logging.md)
- [auth 设计](./docs/features/auth/design.md)
- [exception 模块](./docs/features/exception/)
- [dict 设计](./docs/features/dict/design.md)
