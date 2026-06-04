# Docker 本地环境

> Docker 仅用于**开发 / 联调 / 样例 / CI**；Maven 发布的 starter 制品**不包含** Docker 逻辑。

## 启动

```bash
cd deploy/docker
cp .env.example .env
docker compose up -d
```

## 服务（P0 占位）

| 服务 | 端口（默认） | 用途 |
|------|--------------|------|
| redis | 6379 | 样例 `docker` profile 连接 |

## 与 Spring 配置

- Profile：`docker`
- 文件：`sample-boot-app` 的 `application-docker.yml`
- 主机名使用 compose **service 名称**（如 `redis`），不要用 `localhost`

组件行为仍由 `component.*` 配置，Docker 只提供基础设施连通性。

## 停止

```bash
docker compose down
```

## 常见问题

- **端口冲突**：修改 `deploy/docker/.env` 中 `REDIS_PORT`。  
- **样例连不上 Redis**：确认已 `-Dspring-boot.run.profiles=docker` 且 compose 已启动。
