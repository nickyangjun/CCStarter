# CCStarter · 公司通用 Spring Boot 组件库

可插拔、可配置的 **Spring Boot Starter 积木组件**  monorepo。业务项目通过 **BOM + starter 依赖 + `component.*` 配置** 接入，无需复制粘贴通用能力代码。

| 项 | 说明 |
|----|------|
| 技术基线 | Spring Boot **3.2.x**、Java **17+** |
| 当前阶段 | **P1～P3 可发布**（SNAPSHOT，见 [实施进度 TODO](./组件库实施进度%20TODO.md)） |
| 规范文档 | [Spring Boot 可插拔积木组件建设指南](./Spring Boot 可插拔积木组件建设指南.md)（v2.1，**实施标准，勿当进度板修改**） |

---

## 仓库结构

```
CCStarter/
├── pom.xml                          # 父工程 company-component-parent
├── company-component-bom/           # 对外 BOM，统一依赖版本
├── company-component-samples/
│   └── sample-boot-app/             # 集成验证样例
├── docs/                            # 设计文档、指南、架构决策
├── scripts/                         # 构建、本地运行、冒烟测试、Docker 部署（见下文）
│   ├── build.sh                     # mvn verify + 打 sample JAR
│   ├── run-sample.sh                # 仅启动 sample（前台）
│   ├── test-sample.sh               # 仅 HTTP 冒烟（需已启动）
│   ├── smoke-test.sh                # 一键 build + 测 + 停（CI 同款）
│   ├── deploy-sample-docker.sh      # sample Docker 部署 + 测试
│   └── lib/                         # common.sh、smoke-cases.sh
├── deploy/docker/                   # 本地 / CI 中间件（Docker Compose）
├── deploy/sample/                   # sample-boot-app 镜像与 compose
├── CHANGELOG.md
├── common-exception-autoconfigure/
├── common-exception-spring-boot-starter/
├── common-auth-autoconfigure/       # P2 JWT + Security
├── common-auth-spring-boot-starter/
├── common-log-autoconfigure/        # P3 TraceId / 请求日志 / 操作日志 SPI
├── common-log-spring-boot-starter/
└── …                              # P4+：file 等
```

每个业务能力（鉴权、日志、异常等）在 P1 之后按 **autoconfigure + starter** 双模块追加，详见建设指南第二节。

---

## 快速开始

### 环境要求

- JDK **17+**
- Maven **3.9+**
- （可选）Docker，用于 `docker` profile 联调中间件

首次使用请赋予执行权限：

```bash
chmod +x scripts/*.sh scripts/lib/*.sh
```

### 脚本一览

| 脚本 | 用途 |
|------|------|
| [scripts/build.sh](./scripts/build.sh) | `mvn clean verify` + 打包 `sample-boot-app` JAR |
| [scripts/run-sample.sh](./scripts/run-sample.sh) | **仅启动** sample（前台，控制台可见 `[traceId=…] [userId=…]`） |
| [scripts/test-sample.sh](./scripts/test-sample.sh) | **仅测试** HTTP 冒烟（要求 sample 已启动，可反复执行） |
| [scripts/smoke-test.sh](./scripts/smoke-test.sh) | **一键全流程**：build → 后台启动 → test → 停进程（与 CI 一致） |
| [scripts/deploy-sample-docker.sh](./scripts/deploy-sample-docker.sh) | 构建镜像并 `docker compose` 启动，再跑 `test-sample.sh` |

公共库（勿直接执行）：`scripts/lib/common.sh`、`scripts/lib/smoke-cases.sh`。

更细说明见 **[scripts/README.md](./scripts/README.md)**。

### 推荐用法

**提交前 / CI（一条命令）**

```bash
./scripts/smoke-test.sh
```

**本地开发（两终端）**

```bash
# 终端 1
./scripts/run-sample.sh

# 终端 2（改代码并重启 run-sample 后可多次执行）
./scripts/test-sample.sh
```

**仅编译（与 CI 单测一致）**

```bash
./scripts/build.sh
```

### 冒烟测试覆盖

`test-sample.sh` / `smoke-test.sh` 会校验 exception、auth、log 核心流程：

| # | 场景 |
|---|------|
| 1 | 带 `X-Trace-Id` 透传（默认 `smoke-trace-0001`，模拟网关） |
| 2 | **无** `X-Trace-Id` 时服务自建 traceId（每次请求新 UUID） |
| 3 | 登录获取 JWT |
| 4 | 带 Token 访问受保护接口 |
| 5 | 无 Token → 401 + 响应体 `traceId` |
| 6 | 500 / 404 统一错误 JSON 含 `traceId` |
| 7 | `POST /api/sample/orders` 操作日志链路 |
| 8 | 后台启动时校验日志文件（仅 `smoke-test.sh`） |

常用环境变量：

| 变量 | 默认 | 说明 |
|------|------|------|
| `SMOKE_PORT` | `18080` | sample 端口 |
| `SMOKE_TRACE_ID` | `smoke-trace-0001` | 模拟网关 TraceId（用例 1/3+） |
| `BUILD_FIRST` | `1` | 是否先执行 `build.sh` |
| `SMOKE_SKIP_START` | — | 仅 `smoke-test`；一般用 `test-sample` 代替已启动场景 |

示例：`SMOKE_TRACE_ID=my-gateway-id ./scripts/test-sample.sh`

### Docker

**sample 应用镜像（组件联调）**

```bash
./scripts/deploy-sample-docker.sh
```

**中间件（Redis 等，可选）**

使用 Docker 中间件时（先启动 compose，见 [docs/guides/docker.md](./docs/guides/docker.md)）：

```bash
cd deploy/docker && docker compose up -d
cd company-component-samples/sample-boot-app
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## 业务项目如何接入

### 1. 依赖（BOM + starter）

1. 在业务 `pom.xml` 中 **import** `company-component-bom`。
2. 声明所需 `common-*-spring-boot-starter`（**不要**直接依赖 autoconfigure）。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.company.component</groupId>
            <artifactId>company-component-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-exception-spring-boot-starter</artifactId>
    </dependency>
    <!-- 鉴权：建议与 exception 一并引入，401/403 走统一 JSON -->
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-auth-spring-boot-starter</artifactId>
    </dependency>
    <!-- 链路日志：建议与 exception 一并引入，错误体带 traceId -->
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-log-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

Maven 坐标见建设指南 **第十四节**；[鉴权接入](./docs/features/auth/integration.md)、[日志接入](./docs/features/log/integration.md)。

### 2. 基础配置（各环境共用）

`component.*` 与 Spring Boot 原生配置相同，写在业务项目的 `application.yml`：

```yaml
component:
  exception:
    enabled: true
    include-path: true
    expose-stack-trace: false
    default-error-code: INTERNAL_ERROR
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}
    expire-minutes: 120
    whitelist:
      - /actuator/**
      - /api/public/**
  log:
    enabled: true
    trace:
      allow-local-generate: true
      response-header: true
```

登录接口由业务实现，注入 `JwtService` 签发 Token。网关须透传 **`X-Trace-Id`**；Logback 推荐 `[traceId=%X{tid}] [userId=%X{userId}]`。详见 [auth](./docs/features/auth/integration.md) / [log](./docs/features/log/integration.md) 接入文档。

### 3. 多环境（测试 / 正式 / 本地）

**支持分环境配置**，无需改组件代码；通过 **Profile + `application-{profile}.yml`**（或 Nacos/Apollo 分 namespace）区分即可。

| Profile | 用途 | 说明 |
|---------|------|------|
| `test` | 测试环境 | 行为宜与 prod 对齐；`expose-stack-trace` 保持 `false` |
| `prod` | 正式环境 | 密钥用环境变量/配置中心；**禁止** `expose-stack-trace: true` |
| `docker` | 本地中间件 | 只改 Redis/DB 等连接地址，不替代 `component.*` 行为 |

激活环境：

```bash
java -jar app.jar --spring.profiles.active=prod
# 或 export SPRING_PROFILES_ACTIVE=test
```

业务项目建议文件结构：

```
src/main/resources/
├── application.yml           # 公共默认（component.* 主干）
├── application-test.yml      # 测试环境覆盖项
├── application-prod.yml      # 正式环境覆盖项
└── application-docker.yml    # 本地 Docker（可选）
```

示例（`exception` + `auth` + `log`）：

```yaml
# application-test.yml / application-prod.yml
component:
  exception:
    enabled: true
    expose-stack-trace: false
    include-trace-id: true
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}
  log:
    enabled: true
```

完整约定与配置中心说明见建设指南 **[§5.6 多环境配置](./Spring Boot 可插拔积木组件建设指南.md)**；样例见 `company-component-samples/sample-boot-app/src/main/resources/application-*.yml`。

---

## 文档索引

| 文档 | 用途 |
|------|------|
| [建设指南 v2.1](./Spring Boot 可插拔积木组件建设指南.md) | 架构、规范、检查清单定义 |
| [实施进度 TODO](./组件库实施进度 TODO.md) | **进度勾选、阻塞、日志** |
| [docs/README.md](./docs/README.md) | 文档中心阅读顺序 |
| [docs/architecture/team-decisions.md](./docs/architecture/team-decisions.md) | 团队锁定决策 |
| [docs/guides/getting-started.md](./docs/guides/getting-started.md) | 克隆、构建、发布 |
| [scripts/README.md](./scripts/README.md) | 构建 / 运行 / 冒烟 / 部署脚本 |
| [auth 业务接入](./docs/features/auth/integration.md) | JWT、白名单、登录集成 |
| [log 业务接入](./docs/features/log/integration.md) | TraceId、MDC、操作日志 SPI |
| [MDC 约定](./docs/architecture/logging.md) | `tid` / SkyWalking / 与 userId 分离 |

---

## 路线图（摘要）

| 阶段 | 内容 | 状态 |
|------|------|------|
| P0 | 父工程、BOM、docs、Docker、sample | ✅ 已完成 |
| P1 | common-exception | ✅ 可发布（SNAPSHOT） |
| P2 | common-auth | ✅ 可发布（SNAPSHOT，需配 exception） |
| P3 | common-log | ✅ 可发布（SNAPSHOT，需配 exception） |
| P4～P6 | file / dict / sms | 未开始 |

明细以 [组件库实施进度 TODO](./组件库实施进度%20TODO.md) 为准。

---

## 参与贡献

1. 规范变更 → 评审后更新 **建设指南** 与 CHANGELOG。  
2. 日常进度 → 只更新 **实施进度 TODO** 与 `docs/features/{feature}/`。  
3. 新模块 → 先完成建设指南 **阶段 0 + 附录 C**，再提交代码。

---

## License

内部项目；`groupId` 占位为 `com.company.component`，实施时请在 `docs/architecture/team-decisions.md` 替换为正式坐标。
