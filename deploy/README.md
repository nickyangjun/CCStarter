# deploy · 部署与本地环境

> CCStarter 是 **Spring Boot 组件库 monorepo**，本目录用于**本地 Docker 联调**与**组件库发布**说明，**不是**业务系统的生产部署中心。

## 目录结构

```
deploy/
├── README.md                 # 本文件
├── docker/
│   ├── infra/                # 仅中间件（MySQL + Redis）
│   │   ├── docker-compose.yml
│   │   └── .env.example
│   └── stack/                # 全栈：中间件 + sample-boot-app 容器
│       ├── Dockerfile
│       └── docker-compose.yml
└── release/                  # 组件库 Maven 发布与版本晋级（见 release/README.md）
```

应用源码在 `company-component-samples/sample-boot-app/`，**不在** `deploy/` 下重复放置业务代码。

## 怎么选

| 场景 | 做法 |
|------|------|
| 无 Docker，快速验证组件 | 仓库根目录 `./scripts/run-sample.sh`（H2 + 内存缓存） |
| 宿主机 Maven + 容器中间件 | `deploy/docker/infra` 起 MySQL/Redis，本机 `mvn spring-boot:run`（见下） |
| 全 Docker、无需本机 Maven（推荐 demo / CI） | `./scripts/deploy-sample-docker.sh` 或 `deploy/docker/stack` |
| 发布 SNAPSHOT / RELEASE 到私服 | [release/README.md](./release/README.md) |

## docker/infra · 宿主机开发

仅启动 MySQL + Redis，应用在**宿主机**运行：

```bash
cd deploy/docker/infra
cp .env.example .env   # 按需改端口
docker compose up -d
```

`docker` profile 默认主机名为 compose 服务名（`mysql` / `redis`），在宿主机上须用 **127.0.0.1** 覆盖：

```bash
cd company-component-samples/sample-boot-app
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/sample?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME=sample
export SPRING_DATASOURCE_PASSWORD=sample
export SPRING_DATA_REDIS_HOST=127.0.0.1
export SPRING_DATA_REDIS_PORT=6379
mvn spring-boot:run -Dspring-boot.run.profiles=docker,test
```

配置说明见 `application-docker.yml` 与 [docs/guides/docker.md](../docs/guides/docker.md)。

## docker/stack · 全栈容器

MySQL + Redis + sample，容器内多阶段 Maven 编译：

```bash
docker compose -f deploy/docker/stack/docker-compose.yml up -d --build
# 或
./scripts/deploy-sample-docker.sh
```

| 服务 | 默认端口 | 用途 |
|------|----------|------|
| mysql | 3307 → 3306 | Flyway + `JdbcDictDataProvider` |
| redis | 6379 | 字典缓存 + login-redis 验证码 Store |
| sample-boot-app | 18080 | 样例应用（`SPRING_PROFILES_ACTIVE=docker,test`） |

环境变量：`MYSQL_PORT`、`REDIS_PORT`、`SAMPLE_HOST_PORT`、`DOCKER_SKIP_TESTS`（见 [scripts/README.md](../scripts/README.md)）。

## 与 scripts/ 的关系

| 脚本 | 指向 |
|------|------|
| `scripts/deploy-sample-docker.sh` | `deploy/docker/stack/docker-compose.yml` |
| `scripts/run-sample.sh` / `smoke-test.sh` | 宿主机 JAR，不依赖本目录 |

## 相关文档

- [docs/guides/docker.md](../docs/guides/docker.md) — Profile、常见问题  
- [deploy/release/README.md](./release/README.md) — Maven 发布  
- [docs/guides/getting-started.md](../docs/guides/getting-started.md) — 克隆与构建
