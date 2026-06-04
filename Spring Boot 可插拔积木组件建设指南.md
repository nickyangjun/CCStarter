# Spring Boot 可插拔积木组件建设指南（企业标准化落地版）

## 文档基础信息

| 项 | 内容 |
|----|------|
| 文档定位 | 团队级**唯一实施标准**。编码、评审、发布、接入均以本文为准 |
| 适用范围 | 内部通用组件库（多模块 Maven 工程），供各业务 Spring Boot 项目通过依赖 + 配置接入 |
| 版本基线 | **Spring Boot 3.2.x**（全库统一锁定），**Java 17+** |
| 文档版本 | **v2.1**（纠错优化版） |
| 规范升级说明 | 修复 Spring Boot 3 兼容性表述、补全缺失约束、统一可落地无歧义规范 |

---

## 如何使用本文档（编码前必读）

| 阶段 | 阅读章节 | 产出物 |
|------|----------|--------|
| 立项 / 新能力规划 | 第一节、第二节、**附录 B（边界）**、**第十六节（路线图）** | 能力边界说明、`docs/features/{feature}/` 设计草稿 |
| 编码前 | **第三节（锁定决策）**、第四节～第五节、**附录 A** | 前缀登记、配置项清单、条件装配方案 |
| 编码中 | **第十节（实施清单）**、第七节（包结构）、第八节（SPI）、第十一节（测试） | 代码 + 单测 + 元数据 |
| 发布前 | **第十二节（安全）**、**第十五节 15.4（DoD）**、第十三节（反模式） | CHANGELOG、文档、验收记录 |
| 业务接入 | 第十四节 | 业务项目配置与验收 |

**强制顺序**：未完成 **阶段 0 设计文档 + 附录 C 编码前总检查清单**，不得开始写实现代码（P0 工程骨架除外）。

---

## 一、建设目标与原则

### 1.1 要达成的效果

| 目标 | 说明 | 验收方式 |
|------|------|----------|
| 可复用 | 能力集中在组件库维护，业务项目不复制实现代码 | 业务 pom 仅依赖 starter |
| 可配置 | 行为、密钥、阈值等来自配置或配置中心 | 无硬编码密钥；有 Properties + 元数据 |
| 可插拔 | `enabled=false` 时不注册本组件 Bean/过滤器/端点 | `ApplicationContextRunner` 单测 + 启动日志 |
| 可演进 | 组件库 SemVer 独立发布，业务按需升级 | CHANGELOG + 破坏性变更说明 |
| 可观测 | 启动可知是否加载；运行期异常可定位 | DEBUG 条件报告 + 规范日志 |

### 1.2 必须遵守的工程原则

1. **不猜测**：条件装配、默认行为以实测与启动日志为准；文档与代码不一致时，**先改代码再回写文档**。
2. **不硬编码**：密钥、URL、阈值等不得写死在业务逻辑；生产环境必填项无配置则**启动失败**。
3. **不吞异常**：禁止 `catch` 后静默；组件内错误抛出或纳入统一异常体系（见第九节）。
4. **可测试**：autoconfigure 必有条件装配单测；核心逻辑（解析、过滤、路由）必有单元测试。
5. **不重复造轮子**：优先 Spring 官方能力与团队已有基础包；组件做「粘合、约定、默认实现」。
6. **文档与代码同 PR**：行为或配置变更时，同步更新 `docs/features/{feature}/` 与 CHANGELOG。

### 1.3 组件库与业务系统的边界（什么能放、什么不能放）

| 放在组件库 | 放在业务系统 |
|------------|--------------|
| 通用技术能力：JWT 工具、Security 过滤器模板、操作日志切面框架 | 具体用户表、订单表、业务权限模型 |
| 可配置的默认实现 + SPI/接口扩展点 | 实现 SPI 的业务类（如 `UserDetailsService`） |
| 统一配置前缀 `component.*`、自动装配、条件开关 | 业务 Controller、领域服务、业务流程 |
| 与中间件无关的抽象（如「存储后端接口」） | 调用具体 OSS 账号、业务 bucket 命名规则 |

**禁止**：组件库依赖业务工程的 module；组件库内出现业务包名、业务实体类。

---

## 二、标准架构（固定不变）

### 2.1 模块分层（每个能力一套）

每个通用能力对应 **两个 Maven 子模块**，不得合并为一个「大而全」的 starter：

```
company-component-parent/          # 父工程（packaging=pom）
├── company-component-bom/         # 统一依赖版本（推荐，对外发布）
├── {能力名}-autoconfigure/        # 实现 + 自动配置
└── {能力名}-spring-boot-starter/  # 对外入口（无 Java 代码）
```

| 模块 | 职责 | 是否包含 Java 代码 |
|------|------|-------------------|
| `{能力名}-autoconfigure` | 配置类、条件装配、Properties、核心实现、SPI 接口 | 是 |
| `{能力名}-spring-boot-starter` | 聚合依赖，给业务方一行引入 | **否** |

### 2.2 命名规范（强制）

| 类型 | 格式 | 示例 |
|------|------|------|
| 父工程 artifactId | `company-component-parent` | — |
| BOM artifactId | `company-component-bom` | — |
| 自动配置模块 | `{domain}-{feature}-autoconfigure` | `common-auth-autoconfigure` |
| 对外 starter | `{domain}-{feature}-spring-boot-starter` | `common-auth-spring-boot-starter` |
| 配置前缀 | `component.{feature}`，全小写 | `component.auth` |
| 自动配置类 | `{Feature}AutoConfiguration` | `AuthAutoConfiguration` |
| 配置属性类 | `{Feature}Properties` | `AuthProperties` |
| SPI 接口 | `{Feature}xxx` 或 `{Feature}XxxProvider` | `AuthTokenStore` |

**禁止**：

- 第三方 artifactId 以 `spring-boot-starter-` 开头（官方保留）。
- 自动配置类、Properties 无 `{Feature}` 前缀导致 Bean 冲突。

### 2.3 父工程职责

父 `pom.xml` 仅负责：

- 统一 `groupId`、`version`（推荐 `${revision}` + `flatten-maven-plugin`）
- `dependencyManagement`（Spring Boot BOM + 内部 BOM + 第三方版本）
- `pluginManagement`（compiler、surefire、enforcer 等）
- `<modules>` 列表

**禁止**：

- 在父工程中写业务实现代码  
- 在父工程中**直接声明**第三方依赖（第三方版本仅通过 BOM / `dependencyManagement` 管理）

### 2.4 组件库整体工程结构（目标态）

```
company-component-parent/
├── pom.xml
├── CHANGELOG.md
├── Spring Boot 可插拔积木组件建设指南.md    # 本文件；或 symlink 至 docs/architecture/
├── docs/                                      # 文档中心（见第十五节）
├── deploy/docker/                             # docker-compose、.env.example
├── company-component-bom/
├── company-component-samples/
│   └── sample-boot-app/                       # 集成验证；profile=docker
├── common-*-autoconfigure/
├── common-*-spring-boot-starter/
└── ...
```

---

## 三、编码前必须锁定的团队决策（未锁定不得开工）

以下决策**全库统一**，在 P0 阶段写入 `docs/architecture/team-decisions.md` 并在本文档登记。变更需 ADR 记录。

| 决策项 | 本指南推荐值（默认采用） | 说明 |
|--------|--------------------------|------|
| `component.*.enabled` 默认值 | **`false`** | 引依赖不自动生效；业务显式 `enabled: true` |
| `@ConditionalOnProperty` 的 `matchIfMissing` | **`false`** | 与上表一致：未配置 = 不启用 |
| `groupId` | `com.company.component`（实施时替换为真实值） | 一经发布尽量不修改 |
| Java 包根 | `com.company.component.{feature}` | 与 groupId 对应 |
| Java 版本 | 17 | 与 Boot 3 一致 |
| Spring Boot 版本 | **3.2.x 择一锁定**（禁止与 3.3.x 等小版本混用） | 写入 BOM，子模块不写版本 |
| 配置绑定风格 | `application.yml` + kebab-case | 如 `jwt-secret` → `jwtSecret` |
| 业务接入方式 | 只依赖 starter + BOM 管理版本 | 禁止直接依赖 autoconfigure |
| 样例 / 本地中间件 | Docker Compose + profile `docker` | 见 4.4 |
| 版本号策略 | SemVer：`MAJOR.MINOR.PATCH` | 破坏性变更升 MAJOR |
| 测试框架 | JUnit 5 + AssertJ + `ApplicationContextRunner` | 每个 autoconfigure 模块必用 |
| 最低测试覆盖率（行） | autoconfigure **≥ 70%**（核心包 ≥ 80%） | CI 强制执行 |

> 若业务方强烈要求「引依赖即启用」，须走 ADR，并全库修改 `matchIfMissing` 与文档，**禁止**模块各自为政。

---

## 四、技术基线与依赖策略

### 4.1 版本矩阵

| 技术 | 要求 |
|------|------|
| Spring Boot | **3.2.x**（BOM 统一锁定；禁止跨 3.2 / 3.3 等小版本混用，避免自动配置行为差异） |
| Java | 17+；编译强制 `--release 17` |
| 自动配置注册 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（**Spring Boot 3 唯一合法路径**） |
| 废弃项 | **禁止**使用 `META-INF/spring.factories` 注册自动配置（Boot 3 不兼容） |
| 构建 | Maven 3.9+ |

### 4.2 autoconfigure 模块依赖原则

| 依赖 | 策略 |
|------|------|
| `spring-boot-autoconfigure` | 必需（Spring Boot 3 自动配置核心） |
| `spring-boot-configuration-processor` | **必需**（`optional` 作用域）；生成配置元数据，Boot 3 强依赖 |
| `spring-boot-starter-web` / `security` 等 | 按能力需要；非 Web 场景用 `optional` + `@ConditionalOnWebApplication` |
| 第三方 SDK | 版本仅出现在 BOM；能 `optional` 则 `optional` |
| `spring-boot-starter-test` | `test` 范围，用于 `ApplicationContextRunner` |

### 4.3 starter 模块依赖原则

- **仅**依赖本能力 `{feature}-autoconfigure`（版本 `${project.version}`）
- 禁止在 starter 中声明额外第三方依赖
- **禁止** starter 模块出现任何 **Java 代码**、`application*.yml` 等配置文件（纯依赖聚合）

### 4.4 Docker 开发与本地运行环境

| 项 | 规则 |
|----|------|
| 适用范围 | 开发、联调、样例工程、CI 集成测试；**不**打入 Maven 制品 |
| 编排 | `deploy/docker/docker-compose.yml` + `.env.example` |
| 应用配置 | Profile `docker` + `application-docker.yml`；主机名用 compose service name |
| 与 `component.*` 关系 | Docker 管基础设施连通；组件行为仍由 `component.{feature}` 控制 |
| 敏感信息 | `.env` 不入库；文档见 `docs/guides/docker.md` |

### 4.5 Maven BOM 与发布（P0 必须就绪）

| 项 | 规则 |
|----|------|
| 对外 BOM | `company-component-bom` 管理所有 `common-*` 模块与第三方版本 |
| 业务项目 | `import` BOM，只声明 starter 依赖，不写版本号 |
| 发布物 | autoconfigure、starter、BOM 均发布至团队 Maven 仓库 |
| SNAPSHOT | 仅开发分支；发布 RELEASE 前必须 `mvn clean verify` |
| 源码 | 发布时带 `-sources`、`-javadoc`（内部库推荐） |

### 4.6 CI 最低要求

- `mvn -B clean verify`（含单测、**代码风格检查**、**覆盖率校验**）
- 可选：使用 `deploy/docker` 启动依赖后跑 sample 集成测试
- 禁止合并：单测失败、覆盖率低于第三节阈值、未更新 CHANGELOG（行为变更时）

---

## 五、配置体系标准

> 本节约定配置绑定与 Properties 写法，避免 Boot 3 下元数据缺失、扫描混乱等问题。

### 5.1 统一配置命名空间

所有组件配置挂在 `component` 下，便于配置中心与审计：

```yaml
component:
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}   # 生产从环境变量注入
```

### 5.2 每个组件必须提供的配置项

| 配置项 | 类型 | 默认值（锁定） | 说明 |
|--------|------|----------------|------|
| `enabled` | boolean | **`false`** | 总开关；`false` 时不注册本组件相关 Bean |
| 领域参数 | 各模块定义 | 非敏感可有文档化默认值 | 过期时间、白名单路径等 |
| 敏感参数 | String 等 | **无默认值** | 缺省且 `enabled=true` 时启动校验失败 |

### 5.3 Properties 类规范（Spring Boot 3）

- `@ConfigurationProperties(prefix = "component.{feature}")`
- 类上使用 `@Validated`；敏感/必填字段使用 `@NotBlank`、`@NotNull` 等
- **不在** Properties 的 setter 中写业务副作用
- **全库统一**：在自动配置类上使用 `@EnableConfigurationProperties({Feature}Properties.class)`；**禁止** `@ConfigurationPropertiesScan`（避免包扫描混乱、Bean 重复）
- 编译生成 `spring-configuration-metadata.json`；复杂项补充 `additional-spring-configuration-metadata.json` 描述与示例

### 5.4 配置绑定与自动配置（标准模板）

```java
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
@ConditionalOnProperty(
    prefix = "component.auth",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false // 全库统一：未配置 = 不启用
)
public class AuthAutoConfiguration {
    // @Bean 定义
}
```

### 5.5 配置项生命周期

| 操作 | 规则 |
|------|------|
| 新增配置项 | MINOR 版本；文档 + 元数据 + CHANGELOG |
| 重命名 | 先 `@Deprecated` 保留一版 MINOR，下一大版本删除 |
| 改默认值 | 视为行为变更，CHANGELOG 标明；可能升 MINOR 或 MAJOR |
| 删除配置项 | 仅 MAJOR |

### 5.6 环境差异（dev / test / prod / docker）

| Profile | 用途 |
|---------|------|
| `default` | 本地无 Docker 时的可选配置（**仅允许非敏感**默认值） |
| `docker` | 连接 compose 中间件，本地开发专用 |
| `prod` | 仅通过环境变量 / 配置中心注入密钥；**禁止**弱密钥默认值与测试配置混入 |

组件库代码**不**读取 `spring.profiles.active` 做分支逻辑；差异全部由业务 `application-*.yml` 与 `component.*` 完成。

---

## 六、自动配置（可插拔）标准

> 本节为 Spring Boot 3 落地核心：自动配置类写法、注册文件、条件装配与 Bean 命名。

### 6.1 自动配置类规范

- **必须**使用 `@AutoConfiguration`；**禁止** Spring Boot 2 时代的旧写法（仅靠 `@Configuration` + `spring.factories`）
- 一个能力一个**主**入口 `{Feature}AutoConfiguration`；复杂能力拆分 `XxxSecurityAutoConfiguration` 等，由主类 `@Import`
- **严格禁止** `@ComponentScan` 扫描整个组件包（避免 Bean 失控、冲突、不必要的启动开销）；仅通过 `@Bean` 显式注册

### 6.2 条件装配（组合使用）

| 注解 | 用途 |
|------|------|
| `@ConditionalOnProperty` | `component.{feature}.enabled=true` |
| `@ConditionalOnClass` | 可选第三方类存在（**禁止** `OnClass` 本模块自有类——无实际条件意义） |
| `@ConditionalOnMissingBean` | 业务自定义实现优先，形成覆盖机制 |
| `@ConditionalOnWebApplication` | Servlet / Reactive 区分 |
| `@ConditionalOnBean` | 依赖其他组件或业务 Bean |
| `@ConditionalOnResource` | 可选配置文件存在时启用 |

### 6.3 Bean 定义规范

- 默认实现用 `@Bean`，**绝对禁止**工具类同时 `@Component` + `@Bean`（重复注册）
- 可被覆盖的 Bean 必须 `@ConditionalOnMissingBean`
- **对外暴露的 Bean 必须指定唯一名称**，如 `@Bean("componentAuthJwtUtil")`，避免跨模块/跨组件命名冲突
- **无状态**工具类优先纯静态或独立类，由配置类 `@Bean` 统一注入依赖

### 6.4 自动配置顺序

与 Security、WebMvc 等集成时，必须显式声明顺序，避免过滤器链错乱：

```java
@AutoConfiguration(after = SecurityAutoConfiguration.class)
// 或 before = ...
```

- 过滤器、Interceptor 使用 `Ordered` / `@Order` 常量，**常量集中**在 `{feature}.support.OrderConstants`
- 顺序值在 `docs/features/{feature}/design.md` 中画图说明，留存评审依据

### 6.5 注册自动配置（Spring Boot 3 唯一路径）

路径：

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

规则：

- 每行一个全限定类名，**一个类一行**
- **禁止**逗号拼接、空行、注释行
- **彻底废弃** `META-INF/spring.factories` 的 `EnableAutoConfiguration` 注册方式（Boot 3 不兼容）

### 6.6 启动期可观测

- 推荐：在 DEBUG 级别输出本组件条件匹配结果（可封装 `ConditionOutcomeLogger`）
- 可选：注册 `BeanPostProcessor` 仅用于开发期诊断（生产默认关闭）
- **禁止**：INFO 级别对每条请求打印组件内部状态（避免日志泛滥）

### 6.7 与 Spring Boot Actuator（可选）

- 可提供 `{feature}HealthIndicator`，仅在 `enabled=true` 时注册
- 健康检查**禁止**依赖外网或强依赖中间件外网连通性；DOWN 时需返回**明确原因与排查提示**

---

## 七、Java 包结构与代码组织

### 7.1 包结构（每个 autoconfigure 模块）

```
com.company.component.{feature}/
├── autoconfigure/          # *AutoConfiguration 自动配置入口
├── properties/             # *Properties 配置绑定类
├── support/                # 内部工具、常量、Order 排序、枚举
├── core/                   # 核心逻辑（与 Spring 解耦优先）
├── web/                    # 过滤器、Interceptor（可选）
├── security/               # Security 配置、过滤器（可选）
└── spi/                    # 对外扩展接口（业务实现）
```

### 7.2 可见性

| 类型 | 可见性 |
|------|--------|
| SPI 接口 | `public`；稳定后**禁止随意变更**签名 |
| 自动配置类 | `public`（需被 Spring 框架加载） |
| 实现类、工具类 | **包私有**；仅测试必要时报备后使用 `public` |

### 7.3 依赖方向

`spi` → `core` → `autoconfigure`；**禁止** `core` 依赖 `web` 具体框架类型（必要时用接口抽象解耦）。**禁止**循环依赖。

---

## 八、SPI 与扩展点标准

每个能力若需业务接入，**必须**定义 SPI，而不是让业务继承组件内部类。

| 规则 | 说明 |
|------|------|
| 接口位置 | `{feature}/spi/` 包 |
| 默认行为 | 无 SPI 实现时：要么提供**安全默认**（如拒绝访问），要么**不注册**依赖该 SPI 的 Bean；**禁止**因缺 SPI 导致应用启动失败 |
| 注册方式 | 业务 `@Bean` 实现接口 + 组件 `@ConditionalOnBean(Spi.class)` |
| 文档 | README 与 `design.md` 必须标注 SPI 方法语义、线程安全、异常约定、**入参/出参规范** |

**示例模式**：

```java
// 组件定义
public interface OperationLogRecorder {
    void record(OperationLogEntry entry);
}

// 业务实现（业务侧扩展，不修改组件源码）
@Bean
public OperationLogRecorder operationLogRecorder() {
    return new BusinessOperationLogRecorder();
}
```

---

## 九、多组件协作、日志与异常

### 9.1 多组件依赖关系

| 规则 | 说明 |
|------|------|
| 组件间依赖 | 通过 `optional` 依赖 + `@ConditionalOnClass` / `@ConditionalOnBean`；**杜绝**硬依赖与循环依赖 |
| 装配顺序 | `AutoConfiguration.imports` **文件顺序无效**；必须用 `@AutoConfiguration(before= / after=)` 显式控制 |
| 配置前缀 | 各模块独立 `component.{feature}`，**禁止**共用模糊前缀导致配置混淆 |

### 9.2 日志规范

| 项 | 规则 |
|----|------|
| Logger | 使用 SLF4J；按类声明 Logger；**禁止**静态全局「万能」Logger 打所有组件日志 |
| 内容 | 记录组件名、关键操作结果、异常栈；**强制脱敏**：禁止完整 Token、密码、AK/SK、手机号、身份证号等 |
| 级别 | 故障 `ERROR`；装配结果 `DEBUG`；正常业务链路**禁止**刷屏 `INFO` |
| 关联 ID | 若与 log 组件协作，遵循 MDC 键名在 `docs/architecture/logging.md` 统一登记 |

### 9.3 异常与错误语义

- 组件内部：抛出明确自定义异常或统一基础异常（与 `common-exception` **强对齐**）
- **禁止**：捕获后仅 `log.error` 不抛出（静默吞异常）；**仅业务层**可将异常转换为可处理的返回值，且须在文档说明
- HTTP 响应体、错误码**统一**由 `common-exception` 管控；auth、log 等组件**禁止**自定义多套 JSON 格式

---

## 十、单个能力模块实施清单（对照用）

每新增 `{feature}`，复制本节逐项打勾。**阶段 0 完成前禁止写实现代码。**

### 阶段 0：需求与设计（编码前闸门 · 一票否决）

- [ ] 在 **附录 A** 登记 `component.{feature}` 前缀
- [ ] 编写 `docs/features/{feature}/README.md`（一期范围：做 / 不做）
- [ ] 编写 `docs/features/{feature}/design.md`（类图、流程图、条件装配、SPI、Order）
- [ ] 评审：与 Security、Web、DB 的边界与冲突方案
- [ ] 配置项清单（含是否敏感、默认值、校验规则）评审通过
- [ ] 通过 **附录 C 编码前总检查清单**

### 阶段 1：工程骨架

- [ ] 父工程 `<modules>` 增加 autoconfigure + starter
- [ ] BOM 登记模块坐标与第三方版本
- [ ] starter 仅依赖 autoconfigure，`${project.version}`

### 阶段 2：配置层

- [ ] `{Feature}Properties` + 校验 + 元数据
- [ ] `enabled` 默认 `false`，`matchIfMissing = false`

### 阶段 3：核心实现

- [ ] `core` / `spi` 实现；不依赖业务表
- [ ] 单测覆盖核心逻辑

### 阶段 4：自动配置

- [ ] `{Feature}AutoConfiguration` + 条件注解 + 顺序
- [ ] `AutoConfiguration.imports` 注册

### 阶段 5：测试

- [ ] `ApplicationContextRunner`：`enabled` true/false、MissingBean 覆盖
- [ ] 覆盖率达标（见第三节）
- [ ] sample 工程（或模块内测试应用）可手动/CI 验证

### 阶段 6：文档

- [ ] 模块 README + `docs/features/{feature}/` 与代码一致
- [ ] `application.yml` / `application-docker.yml` 示例片段

### 阶段 7：发布

- [ ] `mvn clean verify`、CHANGELOG、版本号 SemVer
- [ ] 发布 BOM + starter + autoconfigure

---

## 十一、测试标准

### 11.1 必测场景（每个 autoconfigure 模块）

| # | 场景 | 断言 |
|---|------|------|
| 1 | 无 `enabled` 配置 | 本组件 Bean 不存在 |
| 2 | `enabled=false` | 本组件 Bean 不存在 |
| 3 | `enabled=true` + 必填项齐全 | 默认 Bean 存在 |
| 4 | `enabled=true` + 缺必填项 | 上下文启动失败或明确异常 |
| 5 | 业务注册同类型 Bean | 默认 Bean 不存在（`@ConditionalOnMissingBean`） |
| 6 | 非 Web 环境（如适用） | Web 相关 Bean 不注册 |

### 11.2 推荐工具

- `ApplicationContextRunner` + `@EnableConfigurationProperties`
- AssertJ 断言
- Mockito 模拟 SPI 外部依赖

### 11.3 集成测试

- `sample-boot-app` 引用 starter，`@SpringBootTest` 验证端到端
- Docker profile 在 CI 可选执行（文档说明命令）

---

## 十二、安全与生产清单

- [ ] 密钥、AK/SK 来自环境变量或配置中心，**不进 Git**
- [ ] `enabled` 默认 `false`，生产显式开启
- [ ] 引入 Security 时：`enabled=false` 不注册本组件 Security 相关配置；文档说明与 Boot 默认 Security 的关系
- [ ] 日志脱敏：Token、密码、身份证号等
- [ ] 依赖漏洞扫描（CI `dependency-check` 或等价工具）
- [ ] 升级第三方（如 jjwt）走 CHANGELOG + 回归单测

---

## 十三、反模式（禁止）

| 反模式 | 正确做法 |
|--------|----------|
| 未写设计文档直接编码 | 先阶段 0 + 附录 C |
| starter 模块写 Java 代码 | 代码仅在 autoconfigure |
| 业务依赖 autoconfigure | 只依赖 starter + BOM |
| 无 `enabled` 或 `matchIfMissing=true` 导致默认全开 | 统一 false |
| 使用 `spring.factories` 注册自动配置 | 仅用 `AutoConfiguration.imports` |
| 使用 `@ConfigurationPropertiesScan` | 仅用 `@EnableConfigurationProperties` |
| `@ComponentScan` 扫全包 | 显式 `@Bean` |
| starter 含 Java 或 yml | starter 仅 pom 依赖 |
| 父 pom 直接声明第三方依赖 | 版本进 BOM |
| 工具类 `@Component` + `@Bean` 重复 | 只 `@Bean` |
| 公共 Bean 无唯一名称 | `@Bean("componentXxxYyy")` |
| `OnClass` 本模块类 | `OnProperty` / 外部可选依赖 |
| 缺 SPI 导致启动失败 | 安全默认或不注册 Bean |
| 组件依赖业务 module | SPI 反转依赖 |
| 每个模块自定义异常 JSON | 收敛到 exception 组件 |
| 复制代码到业务再改 | 回提组件库发布新版本 |

---

## 十四、业务项目接入标准

### 14.1 依赖引入

```xml
<!-- 业务 pom：import BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.company.component</groupId>
            <artifactId>company-component-bom</artifactId>
            <version>${company-component.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-auth-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 14.2 配置模板

```yaml
component:
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}
```

### 14.3 覆盖与扩展

- 提供同类型 `@Bean` → 组件默认让路
- 完全自研：不引 starter 或 `enabled: false`

### 14.4 业务侧验收

与第十节阶段 5 场景一致；在业务环境执行一遍并记录。

---

## 十五、文档与制品配套

### 15.1 双层文档

| 层级 | 位置 | 内容 |
|------|------|------|
| 简明 | `{module}/README.md` | 坐标、配置表、开关、FAQ、链到 docs |
| 详尽 | `docs/` | 架构、设计、流程图、Docker、ADR |

### 15.2 `docs/` 目录结构（P0 创建）

```
docs/
├── README.md                      # 阅读顺序索引
├── architecture/
│   ├── team-decisions.md          # 第三节决策落地
│   ├── config-prefix-registry.md  # 附录 A 同步副本
│   └── logging.md                 # MDC 等约定（可选）
├── guides/
│   ├── getting-started.md
│   └── docker.md
├── features/{feature}/
│   ├── README.md
│   ├── design.md
│   └── diagrams/
└── adr/
```

### 15.3 与 PR 的绑定

| 变更类型 | 必须更新 |
|----------|----------|
| 新能力 | `docs/features/{feature}/`、附录 A、CHANGELOG |
| 配置项 | 元数据、README 配置表、CHANGELOG |
| 破坏性变更 | CHANGELOG + 迁移说明 + MAJOR 版本 |

### 15.4 完成定义（DoD）

合并主分支前：

- [ ] **第十节**对应阶段清单已全部勾选
- [ ] `mvn clean verify` 通过
- [ ] 文档与配置元数据已更新
- [ ] 若改默认值或条件装配，已标 CHANGELOG 且评审通过

---

## 十六、能力模块路线图（建议分期）

| 优先级 | 模块 | 配置前缀 | 一期范围 | 注意点 |
|--------|------|----------|----------|--------|
| P0 | 骨架 + BOM + sample + docs + docker | — | 工程、文档、CI、发布 | 无业务逻辑 |
| P1 | common-exception | `component.exception` | 统一响应体、错误码 | 与 `@ControllerAdvice` 协调 |
| P2 | common-auth | `component.auth` | JWT、Security 链、白名单 | `enabled=false` 零 Security 侵入 |
| P3 | common-log | `component.log` | 操作日志切面 | SPI 落库 |
| P4 | common-file | `component.file` | 上传、OSS 抽象 | SDK optional |
| P5 | common-dict | `component.dict` | 字典缓存 | 数据源 SPI |
| P6 | common-sms | `component.sms` | 短信通道抽象 | 厂商 optional |

每个模块上线前：**第十节阶段 0～7 + 附录 C** 全部完成。

---

## 附录 A：配置前缀注册表

| 模块 | 前缀 | 负责人 | 状态 | 设计文档 |
|------|------|--------|------|----------|
| auth | `component.auth` | — | 待建设 | `docs/features/auth/` |
| log | `component.log` | — | 待建设 | `docs/features/log/` |
| file | `component.file` | — | 待建设 | `docs/features/file/` |
| exception | `component.exception` | — | 待建设 | `docs/features/exception/` |
| dict | `component.dict` | — | 待建设 | `docs/features/dict/` |
| sms | `component.sms` | — | 待建设 | `docs/features/sms/` |

> 新增模块：**先登记本表，再创建 `docs/features/{feature}/`，最后才允许编码。**

---

## 附录 B：能力边界自检表（阶段 0 用）

对每个 `{feature}` 回答「是 / 否」并写入设计文档：

1. 该能力是否可在 3 个以上业务项目中复用？  
2. 是否可以不依赖具体业务表？  
3. 关闭 `enabled` 后，是否保证无残留过滤器/端点？  
4. 是否与现有 `common-*` 模块冲突？如何解决顺序？  
5. 敏感配置是否已全部列出且无生产默认值？  
6. SPI 是否已定义且业务可实现？  
7. 是否已画流程图（请求链 / 异步链）？  
8. 单测场景是否覆盖第十一节 6 项？

**任一项为「否」且无法在本期解决**：缩小一期范围或拆分模块。

---

## 附录 C：编码前总检查清单（一票否决）

**以下全部满足后，方可创建 `{feature}` 实现类（Java）：**

- [ ] 附录 A 已登记前缀  
- [ ] `docs/features/{feature}/README.md` 与 `design.md` 已提交  
- [ ] 第三节团队决策已确认（或已写入 `team-decisions.md`）  
- [ ] 配置项清单（含 `enabled`、敏感项、默认值）已评审  
- [ ] 条件装配方案（含 `matchIfMissing=false`）已写明  
- [ ] SPI 接口草案（如需要）已评审  
- [ ] 与 Security / Web 的 Order 策略已写明  
- [ ] 第十一节测试场景已规划（可先写测试类骨架）  
- [ ] 附录 B 八项均为「是」或已记录例外与风险  

---

*维护：组件库负责人 | 变更请更新 CHANGELOG 与 `docs/adr/`*
