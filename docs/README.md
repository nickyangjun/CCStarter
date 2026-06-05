# 文档中心

## 阅读顺序

1. [仓库 README](../README.md) — 项目概览与快速构建  
2. [建设指南 v2.1](../Spring Boot 可插拔积木组件建设指南.md) — **实施标准（勿写进度）**  
3. [实施进度 TODO](../组件库实施进度 TODO.md) — **进度勾选**  
4. [architecture/team-decisions.md](./architecture/team-decisions.md) — 团队锁定决策  
5. [guides/getting-started.md](./guides/getting-started.md) — 克隆、构建、本地安装  
6. [guides/docker.md](./guides/docker.md) — Docker Compose 与 `docker` profile  
7. [deploy/README.md](../deploy/README.md) — deploy 目录决策表；[release](../deploy/release/README.md) — Maven 发布  

## 目录说明

| 目录 | 内容 |
|------|------|
| `architecture/` | 团队决策、配置前缀注册表、[日志 MDC 约定](./architecture/logging.md) |
| `guides/` | 操作指南 |
| `features/{feature}/` | [exception](./features/exception/)、[auth](./features/auth/)（含 [login-redis 接入](./features/auth/integration.md#33-业务-spi必填--按需)）、[log](./features/log/)、[dict](./features/dict/) |
| `adr/` | 架构决策记录（重大变更时新增） |
