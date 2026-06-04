# 组件库实施进度 TODO

> **用途**：跟踪**实施进度、勾选状态、备注与阻塞**；随开发随时更新本文件。  
> **不用于**：修改规范条文——标准仍以 [Spring Boot 可插拔积木组件建设指南.md](./Spring Boot%20可插拔积木组件建设指南.md)（v2.1）为准。  
> **最后更新**：2026-06-04 | **当前阶段**：P0 ✅ 已完成

---

## 一、两份文档如何分工

| 文档 | 角色 | 何时更新 |
|------|------|----------|
| **建设指南** | 团队标准：架构、规则、检查项定义 | 仅当规范/流程变更时（走评审，升 doc 版本） |
| **本 TODO** | 执行看板：谁在做、做到哪、是否完成 | 每次开发会话开始/结束、PR 合并前后 |

**原则**：指南 = 「应该怎样做」；TODO = 「我们做到哪了」。

---

## 二、总体进度一览

| 阶段 | 范围 | 状态 | 目标完成 |
|------|------|------|----------|
| **P0** | 父工程、BOM、docs、docker、sample、CI、首次 `install` | ✅ 已完成 | 2026-06-04 |
| P1 | common-exception | ⬜ 未开始 | — |
| P2 | common-auth | ⬜ 未开始 | — |
| P3 | common-log | ⬜ 未开始 | — |
| P4 | common-file | ⬜ 未开始 | — |
| P5 | common-dict | ⬜ 未开始 | — |
| P6 | common-sms | ⬜ 未开始 | — |

**图例**：⬜ 未开始 | 🟡 进行中 | ✅ 已完成 | ⏸ 阻塞

---

## 三、P0：工程骨架（当前焦点）

> 对应建设指南 **第十六节 P0**。P0 **允许**在无业务模块时先写骨架代码；业务模块（P1+）须先完成各模块 **阶段 0 + 附录 C**。

### 3.1 团队决策落地（编码前）

- [x] 确认真实 `groupId`（当前占位：`com.company.component`）
- [x] 确认 Spring Boot 锁定版本（**3.2.12**）
- [x] 创建 `docs/architecture/team-decisions.md`
- [x] 创建 `docs/architecture/config-prefix-registry.md`

### 3.2 Maven 父工程与 BOM

- [x] 创建根目录 `pom.xml`（`company-component-parent`，`${revision}` + flatten）
- [x] 创建 `company-component-bom/pom.xml`（独立 BOM，对外发布用）
- [x] 父工程 `dependencyManagement`：Spring Boot BOM
- [x] `pluginManagement`：compiler `--release 17`、surefire
- [x] 根目录 `CHANGELOG.md`

> **说明**：BOM 模块不继承 parent，避免 reactor 内 `import` 循环；版本号与 `${revision}` 保持同为 `1.0.0-SNAPSHOT`。

### 3.3 文档与 Docker 骨架

- [x] 创建 `docs/README.md`
- [x] 创建 `docs/guides/getting-started.md`
- [x] 创建 `docs/guides/docker.md`
- [x] 创建 `deploy/docker/docker-compose.yml`（Redis 占位）
- [x] 创建 `deploy/docker/.env.example`
- [x] `.gitignore`

### 3.4 样例工程 sample-boot-app

- [x] 创建 `company-component-samples/pom.xml`
- [x] 创建 `sample-boot-app`（Boot 3.2.12 + Java 17）
- [x] `application.yml` + `application-docker.yml`
- [x] 冒烟接口 `/api/sample/ping` + 上下文加载单测

### 3.5 构建与发布验证

- [x] `mvn clean verify` 通过
- [x] `mvn clean install` 通过
- [ ] （可选）配置 CI 流水线占位
- [x] P0 完成：进度日志已记录

### P0 完成标准（DoD）

- [x] 目录结构与建设指南 **第二节 2.4** 一致（尚无 `common-*` 模块）
- [x] 无业务 `common-*` 模块
- [x] 可按 `docs/guides/getting-started.md` 构建

---

## 四、P1～P6：能力模块进度（按模块更新）

每个模块按建设指南 **第十节阶段 0～7** 执行；下表只跟踪**模块级状态**，细则勾选见各模块小节或 `docs/features/{feature}/`。

| 模块 | 前缀 | 阶段 0 设计 | 阶段 1～7 实现 | 发布 | 备注 |
|------|------|---------------|----------------|------|------|
| common-exception | `component.exception` | ⬜ | ⬜ | ⬜ | 建议第一个业务组件 |
| common-auth | `component.auth` | ⬜ | ⬜ | ⬜ | |
| common-log | `component.log` | ⬜ | ⬜ | ⬜ | |
| common-file | `component.file` | ⬜ | ⬜ | ⬜ | |
| common-dict | `component.dict` | ⬜ | ⬜ | ⬜ | |
| common-sms | `component.sms` | ⬜ | ⬜ | ⬜ | |

### 4.1 模块实施模板（复制到下方或 `docs/features/{feature}/`）

新开模块时，复制以下块并改 `{feature}`：

```markdown
#### common-{feature} 详细勾选

**阶段 0（编码前闸门）**
- [ ] 附录 A / config-prefix-registry 已登记
- [ ] docs/features/{feature}/README.md
- [ ] docs/features/{feature}/design.md
- [ ] 附录 B 八项自检
- [ ] 附录 C 编码前总检查清单

**阶段 1～7**
- [ ] 1 工程骨架（*-autoconfigure + *-starter）
- [ ] 2 配置层（Properties + 元数据）
- [ ] 3 核心实现 + 单测
- [ ] 4 自动配置 + AutoConfiguration.imports
- [ ] 5 测试（ApplicationContextRunner + 覆盖率）
- [ ] 6 文档与 yml 示例
- [ ] 7 发布（verify + CHANGELOG）

**验收**
- [ ] 指南第十一节 6 项必测场景
- [ ] 指南第十二节安全清单
- [ ] sample-boot-app 集成验证（如适用）
```

---

## 五、阻塞与风险

| 日期 | 项 | 状态 | 说明 |
|------|-----|------|------|
| — | — | — | 暂无 |

---

## 六、进度日志（按时间追加，勿改建设指南）

| 日期 | 动作 | 结果 | 操作人 |
|------|------|------|--------|
| 2026-06-04 | 创建本 TODO 文档 | 完成 | — |
| 2026-06-04 | 项目 README + P0 骨架 | `mvn verify` / `install` 通过；Boot **3.2.12** | — |

---

## 七、快速链接

- [项目 README](./README.md)
- [建设指南 v2.1](./Spring Boot 可插拔积木组件建设指南.md)
- [团队决策](./docs/architecture/team-decisions.md)
- [前缀注册表](./docs/architecture/config-prefix-registry.md)
