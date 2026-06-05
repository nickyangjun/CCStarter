# 团队锁定决策

> 与 [建设指南](../../Spring Boot 可插拔积木组件建设指南.md) 第三节一致。变更须走 `docs/adr/` 记录。

| 决策项 | 采用值 | 说明 |
|--------|--------|------|
| `component.*.enabled` 默认值 | `false` | 引依赖不自动生效 |
| `@ConditionalOnProperty.matchIfMissing` | `false` | 未配置 = 不启用 |
| `groupId` | `com.company.component` | **占位**，上线前替换为正式坐标 |
| Java 包根 | `com.company.component.{feature}` | |
| Java 版本 | 17 | 编译 `--release 17` |
| Spring Boot 版本 | **3.2.12** | 全库锁定，禁止与 3.3.x 混用 |
| 配置绑定 | `application.yml` + kebab-case | |
| 业务接入 | 仅 starter + BOM | 禁止直接依赖 autoconfigure |
| 本地中间件 | Docker Compose + profile `docker` | 见 [deploy/README.md](../deploy/README.md) |
| 版本策略 | SemVer | 破坏性变更升 MAJOR |
| 测试 | JUnit 5 + AssertJ + `ApplicationContextRunner` | |
| 覆盖率 | autoconfigure ≥ 70%，核心包 ≥ 80% | P1+ 模块在 CI 强制执行 |

**BOM 构建说明**：`company-component-bom` 为独立 `pom`（不继承 parent），避免 Maven reactor 内 BOM import 循环；其 `version` 须与父工程 `${revision}` 手动同步。

**登记日期**：2026-06-04
