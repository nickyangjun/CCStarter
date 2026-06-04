# common-exception · 阶段 0 检查清单

> 对应建设指南 **第十节阶段 0**、**附录 B**、**附录 C**。全部满足后，方可进行 Phase 3 业务实现类开发（Handler、映射逻辑）。

## 附录 A · 前缀登记

- [x] `component.exception` 已写入 [config-prefix-registry.md](../../architecture/config-prefix-registry.md)

## 阶段 0 文档

- [x] [README.md](./README.md)（一期范围）
- [x] [design.md](./design.md)（流程、条件装配、SPI、响应体）
- [x] 与 Security 边界：一期不处理 Security 过滤器链异常
- [x] 配置项清单已列出（见 README 速查表）

## 附录 B · 能力边界自检

| # | 问题 | 结论 |
|---|------|------|
| 1 | 3 个以上项目可复用？ | **是** |
| 2 | 不依赖业务表？ | **是** |
| 3 | `enabled=false` 无残留？ | **是**（不注册 Advice） |
| 4 | 与其他 common 冲突？ | **否**；auth 后续单独设计 Order |
| 5 | 敏感配置无生产弱默认？ | **是** |
| 6 | SPI 已定义？ | **是**（接口草案） |
| 7 | 流程图？ | **是**（design.md Mermaid） |
| 8 | 单测场景已规划？ | **是**（design §9） |

## 附录 C · 编码前总检查

- [x] 附录 A 已登记  
- [x] README + design 已提交  
- [x] 第三节决策：`enabled` 默认 false、`matchIfMissing=false`  
- [x] 配置项清单已评审（本目录 README）  
- [x] 条件装配方案已写明（design §3）  
- [x] SPI 草案已评审（design §6）  
- [x] Order 策略已写明（design §3）  
- [x] 测试场景已规划（design §9）  
- [x] 附录 B 均为「是」  

**阶段 0 结论**：✅ 通过（2026-06-04）

**下一步**：Phase 6～7 文档收尾与发布；核心 Handler / 映射 / sample 集成已完成（Phase 3）。
