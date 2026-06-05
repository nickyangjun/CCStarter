# common-dict · 能力概述

| 项 | 内容 |
|----|------|
| 配置前缀 | `component.dict` |
| Maven starter | `common-dict-spring-boot-starter`（规划） |
| 设计文档 | [design.md](./design.md) |
| 阶段 0 检查 | [phase0-checklist.md](./phase0-checklist.md) |
| 发布状态 | **可发布**（1.0.0-SNAPSHOT；HTTP API 二期） |

---

## 一期目标（做）

- 提供 **`DictService` Bean**：按字典类型读取项列表、`code` → `label` / `value` 转换。
- **缓存层**：内存（默认 / sample 本地）或 **Redis**（`docker` profile / 生产多实例）。
- SPI **`DictDataProvider`**：业务从统一表 **`sys_dict_type` / `sys_dict_item`** 加载数据。
- 缓存失效：`refresh(dictType)` / `refreshAll()`（管理端改库后由业务调用）。
- 与 **common-exception** 协作：字典类型不存在、SPI 失败等映射统一错误码。

## 一期不做

- **HTTP API**（`GET /api/dict/{type}` 等）——二期再加。
- 字典管理后台、CRUD API、导入导出（业务系统）。
- 树形字典、多语言 label、Jackson `@DictLabel` 自动翻译（二期）。
- 独立 `common-redis` 模块（一期在 dict 内 optional Redis 缓存实现）。
- Reactive WebFlux（一期仅 `SERVLET` 环境可引 starter，无 Web 专属逻辑）。

---

## 团队锁定决策（2026-06-05）

| # | 议题 | 结论 |
|---|------|------|
| 1 | 字典表规范 | 全团队统一 **`sys_dict_type` + `sys_dict_item`**（含 `dict_source`、`is_builtin`、`item_value`；见 [design.md §4](./design.md#4-统一字典表规范)） |
| 2 | 一期对外形态 | **仅 `DictService` Bean**，不注册 Controller |
| 3 | sample 缓存 | 默认 **`memory`**（`application.yml`） |
| 4 | Docker 联调缓存 | **`docker` profile 使用 `redis`**（`application-docker.yml` + 已有 Redis compose） |
| 5 | Redis 连接 | 仅用 **`spring.data.redis.*`**（基础设施）；`component.dict.cache.*` 管行为 |
| 6 | 数据源 | **必须**实现 `DictDataProvider`；组件不内置 SQL |
| 7 | 与 login-redis 关系 | 共用 Redis 实例，**key 前缀分离**（`{app}:dict:*` vs 未来 `{app}:login:*`） |
| 8 | 字段命名 | 数据库 **`snake_case`**（`dict_type`）；Java/JSON **`camelCase`**（`dictType`）；见 [design §4.0](./design.md#40-命名规范数据库-vs-java-vs-配置) |

---

## 配置项速查（规划）

| 配置键 | 类型 | 默认 | 说明 |
|--------|------|------|------|
| `enabled` | boolean | `false` | 总开关 |
| `cache.type` | string | `memory` | `memory` \| `redis` |
| `cache.ttl-seconds` | int | `3600` | 缓存 TTL（秒），> 0 |
| `cache.key-prefix` | string | `${spring.application.name}:dict` | Redis key 命名空间 |
| `cache.null-ttl-seconds` | int | `60` | 空结果防穿透短 TTL（可选） |

完整说明见 [design.md](./design.md)。

---

## 业务接入（摘要，一期）

1. 引入 `common-dict-spring-boot-starter`（规划坐标见 design §12）。
2. 按规范建表 `sys_dict_type` / `sys_dict_item`，实现 **`DictDataProvider`** Bean。
3. `component.dict.enabled=true`；生产 `cache.type=redis` + `spring.data.redis.*`。
4. 业务 Controller / 导出 / 报表注入 **`DictService`** 做码表翻译。
5. 管理端改字典后调用 **`dictService.refresh(dictType)`**。

完整接入文档编码后补充 `integration.md`（二期，与 HTTP API 同步或提前）。

---

## 相关文档

- [建设指南 · P5](../../../Spring Boot 可插拔积木组件建设指南.md)
- [配置前缀注册表](../../architecture/config-prefix-registry.md)
- [exception 模块](../exception/README.md)
- [Docker 联调](../../guides/docker.md)
