# common-log · 阶段 0 检查清单

## 附录 A · 前缀登记

- [x] `component.log` 已写入 [config-prefix-registry.md](../../architecture/config-prefix-registry.md)

## 阶段 0 文档

- [x] [README.md](./README.md)
- [x] [design.md](./design.md)
- [x] [logging.md](../../architecture/logging.md)（MDC / TraceId / SkyWalking）
- [x] 团队五项决策已写入 README
- [x] TraceId 仅链路追踪；与 userId 分 MDC、禁止合并（logging.md §1.1）
- [x] 与 exception（traceId 字段）、auth（userId MDC）、网关边界已说明

## 附录 B · 能力边界自检

| # | 问题 | 结论 |
|---|------|------|
| 1 | 3 个以上项目可复用？ | **是** |
| 2 | 不依赖业务表？ | **是**（落库走 SPI） |
| 3 | `enabled=false` 无残留？ | **是**（不注册 Filter/切面） |
| 4 | 与其他 common 冲突？ | **已规划**；错误 JSON 归 exception；MDC 键归 architecture |
| 5 | 敏感配置无生产弱默认？ | **是**（无密钥类配置） |
| 6 | SPI 已定义？ | **是**（`OperationLogRecorder` 草案） |
| 7 | 流程图？ | **是**（design §1） |
| 8 | 单测场景已规划？ | **是**（design §11） |

## 附录 C · 编码前总检查

- [x] 附录 A 已登记
- [x] README + design + logging.md 已提交
- [x] TraceId：网关 `X-Trace-Id` 透传、MDC `tid`、禁止有头时重建、禁止含业务语义/用户信息
- [x] 请求日志仅 SLF4J；操作日志 SPI 异步（文档强制推荐）
- [x] 4xx/5xx 必须带 `traceId`（exception 协作方案已写）
- [x] 组件库内部禁止强依赖 log
- [x] 测试场景已规划

**阶段 0 结论**：✅ 通过（2026-06-04）

**下一步**：Phase 1 模块骨架 → Phase 2 `TraceIdFilter` + exception `traceId` 字段 → Phase 3 请求 SLF4J → Phase 4 操作日志 SPI。
