# common-auth · 阶段 0 检查清单

## 附录 A · 前缀登记

- [x] `component.auth` 已写入 [config-prefix-registry.md](../../architecture/config-prefix-registry.md)

## 阶段 0 文档

- [x] [README.md](./README.md)
- [x] [design.md](./design.md)
- [x] 与 Security / exception / 网关边界已说明
- [x] 配置项清单（含 `jwt-secret` 敏感项）

## 附录 B · 能力边界自检

| # | 问题 | 结论 |
|---|------|------|
| 1 | 3 个以上项目可复用？ | **是** |
| 2 | 不依赖业务表？ | **是**（用户加载走 SPI） |
| 3 | `enabled=false` 无残留？ | **是**（不注册组件 Security Bean） |
| 4 | 与其他 common 冲突？ | **已规划**；错误 JSON 归 exception |
| 5 | 敏感配置无生产弱默认？ | **是**（jwt-secret 无默认值） |
| 6 | SPI 已定义？ | **是**（草案） |
| 7 | 流程图？ | **是** |
| 8 | 单测场景已规划？ | **是**（design §10） |

## 附录 C · 编码前总检查

- [x] 附录 A 已登记
- [x] README + design 已提交
- [x] 团队决策（enabled 默认 false 等）适用
- [x] 条件装配与 `enabled=false` Security 策略已写明
- [x] SPI 草案已记录
- [x] 测试场景已规划

**阶段 0 结论**：✅ 通过（2026-06-04）

**下一步**：Phase 2～3 实现 `AuthProperties`、`JwtService`、`JwtAuthenticationFilter`、`AuthSecurityAutoConfiguration`（**禁止**跳过设计直接写业务登录接口）。
