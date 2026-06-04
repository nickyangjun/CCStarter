# 组件库实施进度 TODO

> **最后更新**：2026-06-04 | **当前阶段**：P2 common-auth 已启动（阶段 0 ✅）

---

## 二、总体进度一览

| 阶段 | 范围 | 状态 |
|------|------|------|
| P0 | 工程骨架 | ✅ 已完成 |
| P1 | common-exception | ✅ **可发布**（1.0.0-SNAPSHOT） |
| **P2** | common-auth | 🟡 阶段 0 + 模块骨架 |
| P3～P6 | log / file / dict / sms | ⬜ 未开始 |

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
- [ ] `AuthProperties` + 自动配置（Phase 2～3）

### 4.2 待实现

- [ ] JwtService、JwtAuthenticationFilter、SecurityFilterChain
- [ ] 单测 + sample 集成
- [ ] 与 exception 401/403 JSON 联调

| 模块 | 阶段 0 | 骨架 | 实现 | 发布 |
|------|--------|------|------|------|
| common-auth | ✅ | ✅ | ⬜ | ⬜ |

---

## 七、进度日志

| 日期 | 动作 | 结果 |
|------|------|------|
| 2026-06-04 | P1 提交 + CI | 可发布 SNAPSHOT |
| 2026-06-04 | P2 auth 阶段 0 + POM 骨架 | 待 Phase 3 编码 |

---

## 八、快速链接

- [auth 设计](./docs/features/auth/design.md)
- [exception 模块](./docs/features/exception/)
