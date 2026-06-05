# common-dict · 阶段 0 检查清单

> 设计文档：[design.md](./design.md) · 概述：[README.md](./README.md)

## 附录 A · 前缀登记

- [x] `component.dict` 已写入 [config-prefix-registry.md](../../architecture/config-prefix-registry.md)

## 阶段 0 文档

- [x] [README.md](./README.md)
- [x] [design.md](./design.md)
- [x] 统一表规范 `sys_dict_type` / `sys_dict_item`（design §4；含 `dict_source`、`is_builtin`、`item_value`、`item_code` 256、`extra_json` TEXT）
- [x] 一期仅 `DictService` Bean、二期 HTTP API 已明确
- [x] sample `memory` / docker `redis` 已记录
- [x] SPI `DictDataProvider` 草案与加载约定
- [x] 与 exception、Redis key 隔离、login-redis 关系已说明

## 附录 B · 能力边界自检

| # | 问题 | 结论 |
|---|------|------|
| 1 | 3 个以上项目可复用？ | **是**（标准码表 + 缓存） |
| 2 | 不依赖业务表？ | **是**（加载走 SPI；表规范仅文档约定） |
| 3 | `enabled=false` 无残留？ | **是**（不注册 DictService / Cache） |
| 4 | 与其他 common 冲突？ | **已规划**；错误 JSON 归 exception；Redis key 与 login 分离 |
| 5 | 敏感配置无生产弱默认？ | **是**（无密钥；`key-prefix` 可配置） |
| 6 | SPI 已定义？ | **是**（`DictDataProvider`） |
| 7 | 流程图？ | **是**（design §2） |
| 8 | 单测场景已规划？ | **是**（design §12） |

## 附录 C · 编码前总检查

- [x] 附录 A 已登记
- [x] README + design 已提交
- [x] 团队决策（表规范、一期 Bean-only、memory/docker 缓存、**snake_case / camelCase 分层**）已写入 design §4.0、§13
- [x] `DictDataProvider` 必需、`enabled=false` 零侵入已定义
- [x] Redis 行为与 `spring.data.redis` 分工已明确
- [ ] 评审通过（负责人签字 / PR 评审）

**阶段 0 结论**：✅ **通过**（2026-06-05），可进入阶段 1（模块骨架）

**下一步**：`common-dict-autoconfigure` + `common-dict-spring-boot-starter` POM → `DictProperties` → `DictService` + 内存缓存。
