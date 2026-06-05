# Docker 本地环境

> Docker 仅用于**开发 / 联调 / 样例 / CI**；Maven 发布的 starter 制品**不包含** Docker 逻辑。

## 方式一：sample 全栈（推荐）

**无需本地 Maven**，在 Docker 内多阶段编译并启动 MySQL + Redis + 应用：

```bash
docker compose -f deploy/sample/docker-compose.yml up -d --build
```

带冒烟（可选）：

```bash
./scripts/deploy-sample-docker.sh              # 默认跑 test-sample
RUN_SMOKE=0 ./scripts/deploy-sample-docker.sh  # 只启动，服务常驻
```

`deploy/sample/docker-compose.yml` 包含：

| 服务 | 端口（默认） | 用途 |
|------|--------------|------|
| mysql | 3307（映射容器 3306） | Flyway 建表 + `JdbcDictDataProvider` |
| redis | 6379 | 字典缓存 + 登录验证码 Store |
| sample-boot-app | 18080 | 样例应用（`SPRING_PROFILES_ACTIVE=docker,test`） |

首次 `up --build` 会下载 Maven 依赖，耗时较长；后续改代码 rebuild 会复用 BuildKit 的 `.m2` 缓存。

停止：

```bash
docker compose -f deploy/sample/docker-compose.yml down
```

## 方式二：仅 Redis 基础设施

```bash
cd deploy/docker
cp .env.example .env
docker compose up -d
```

| 服务 | 端口（默认） | 用途 |
|------|--------------|------|
| redis | 6379 | 本地 `-Dspring-boot.run.profiles=docker` 联调 |

## 与 Spring 配置

- Profile：`docker,test`（compose 设置 `SPRING_PROFILES_ACTIVE`，登录测试码与 `application-test.yml` 相同）
- 文件：`sample-boot-app` 的 `application-docker.yml` + `application-test.yml`
- 主机名使用 compose **service 名称**（`mysql`、`redis`），不要用 `localhost`
- 本地无 Docker 时：默认 `application.yml` 使用 H2 内存库 + `component.auth.login.redis.enabled=false`（内存验证码 Store）

组件行为仍由 `component.*` 配置，Docker 只提供基础设施连通性。

## 常见问题

- **端口冲突**：修改环境变量 `MYSQL_PORT` / `REDIS_PORT` 或 compose 映射。  
- **样例连不上 Redis/MySQL**：确认 `deploy-sample-docker.sh` 或 compose 已启动且 healthcheck 通过。  
- **登录仍用固定码**：docker profile 包含 `test`，验证码走 `application-test.yml`，不依赖 Redis Store。
