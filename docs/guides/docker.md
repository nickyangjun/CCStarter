# Docker 本地环境

> Docker 仅用于**开发 / 联调 / 样例 / CI**；Maven 发布的 starter 制品**不包含** Docker 逻辑。  
> 目录说明见 [deploy/README.md](../deploy/README.md)。

## 方式一：全栈 stack（推荐）

**无需本地 Maven**，在 Docker 内多阶段编译并启动 MySQL + Redis + 应用：

```bash
docker compose -f deploy/docker/stack/docker-compose.yml up -d --build
```

带冒烟（可选）：

```bash
./scripts/deploy-sample-docker.sh              # 默认跑 test-sample
RUN_SMOKE=0 ./scripts/deploy-sample-docker.sh  # 只启动，服务常驻
```

`deploy/docker/stack/docker-compose.yml` 包含：

| 服务 | 端口（默认） | 用途 |
|------|--------------|------|
| mysql | 3307（映射容器 3306） | Flyway 建表 + `JdbcDictDataProvider` |
| redis | 6379 | 字典缓存 + 登录验证码 Store |
| sample-boot-app | 18080 | 样例应用（`SPRING_PROFILES_ACTIVE=docker,test`） |

首次 `up --build` 会下载 Maven 依赖，耗时较长；后续改代码 rebuild 会复用 BuildKit 的 `.m2` 缓存。

停止：

```bash
docker compose -f deploy/docker/stack/docker-compose.yml down
```

## 方式二：仅中间件 infra（宿主机 mvn）

```bash
cd deploy/docker/infra
cp .env.example .env
docker compose up -d
```

| 服务 | 端口（默认） | 用途 |
|------|--------------|------|
| mysql | 3307 | 本地 `docker` profile 联调 |
| redis | 6379 | 字典 Redis 缓存 + login-redis Store |

应用在**宿主机**运行时，须用 `127.0.0.1` 覆盖连接（compose 服务名 `mysql`/`redis` 仅在容器网内有效）：

```bash
cd company-component-samples/sample-boot-app
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/sample?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME=sample
export SPRING_DATASOURCE_PASSWORD=sample
export SPRING_DATA_REDIS_HOST=127.0.0.1
export SPRING_DATA_REDIS_PORT=6379
mvn spring-boot:run -Dspring-boot.run.profiles=docker,test
```

## 与 Spring 配置

- Profile：`docker,test`（stack compose 设置 `SPRING_PROFILES_ACTIVE`；infra 模式启动时自行加 `,test`）
- 文件：`sample-boot-app` 的 `application-docker.yml` + `application-test.yml`
- **stack 模式**：主机名用 compose **service 名称**（`mysql`、`redis`）
- **infra + 宿主机 mvn**：连接地址用 `127.0.0.1` + 映射端口（见上）
- 本地无 Docker：默认 `application.yml` 使用 H2 + `component.auth.login.redis.enabled=false`

组件行为仍由 `component.*` 配置，Docker 只提供基础设施连通性。

## 常见问题

- **端口冲突**：修改环境变量 `MYSQL_PORT` / `REDIS_PORT` / `SAMPLE_HOST_PORT` 或 compose 映射。  
- **infra 模式连不上库**：确认已 export `SPRING_DATASOURCE_*` / `SPRING_DATA_REDIS_*` 为 `127.0.0.1`。  
- **stack 样例连不上 Redis/MySQL**：确认 healthcheck 通过后再看 `sample-boot-app` 日志。  
- **登录仍用固定码**：`test` profile 验证码走 `application-test.yml`，不依赖 Redis Store 发码逻辑。

## 组件库发布

Maven SNAPSHOT / RELEASE 见 [deploy/release/README.md](../deploy/release/README.md)。
