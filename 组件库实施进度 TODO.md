# 组件库实施进度 TODO

> **最后更新**：2026-06-04 | **当前阶段**：P1 common-exception ✅ 核心完成

---

## 二、总体进度一览

| 阶段 | 范围 | 状态 |
|------|------|------|
| P0 | 工程骨架 | ✅ 已完成 |
| **P1** | common-exception | ✅ 核心完成（Phase 3） |
| P2 | common-auth | ⬜ 未开始 |

---

## 四、P1：common-exception

### 4.0～4.1 已完成

- [x] 阶段 0 文档与检查清单
- [x] 双模块骨架、BOM、parent DM
- [x] `ExceptionProperties` + 自动配置
- [x] `ComponentGlobalExceptionHandler` + `ExceptionMappingSupport`
- [x] SPI `ExceptionErrorCodeResolver`
- [x] 单测（映射 + 条件装配）
- [x] sample：`enabled: true` + `ExceptionIntegrationTest`

### 4.2 可选收尾

- [ ] CI 流水线
- [ ] 发布 RELEASE 与业务项目接入验证
- [ ] `MethodArgumentNotValidException` 的 sample 演示（需 validation 依赖）

| 模块 | 阶段 0 | 实现 | 发布 |
|------|--------|------|------|
| common-exception | ✅ | ✅ | ⬜ |

---

## 七、进度日志

| 日期 | 动作 | 结果 |
|------|------|------|
| 2026-06-04 | P1 Phase 3 全局异常 | Handler + 映射 + 3 项集成测试通过 |

---

## 八、快速链接

- [exception 设计](./docs/features/exception/design.md)
- [项目 README](./README.md)
