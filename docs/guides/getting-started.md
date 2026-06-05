# 快速开始

## 1. 克隆与 JDK

```bash
git clone <repository-url>
cd CCStarter
```

- JDK **17+**：`java -version`
- Maven **3.9+**：`mvn -version`

## 2. 全量构建与冒烟（推荐）

在仓库根目录：

```bash
chmod +x scripts/*.sh scripts/lib/common.sh
./scripts/smoke-test.sh
```

等价于：`mvn verify` + 打包 sample + 自动启动并校验 **exception / auth / log / dict** 核心 HTTP 流程。  
脚本说明见 [scripts/README.md](../../scripts/README.md)。

仅编译（与 CI 一致）：

```bash
./scripts/build.sh
```

## 3. 运行样例应用

```bash
./scripts/run-sample.sh
```

或：

```bash
cd company-component-samples/sample-boot-app
mvn spring-boot:run
```

应用已启动时，另开终端只跑测试：

```bash
./scripts/test-sample.sh
```

验证：访问 `http://localhost:18080/api/sample/ping`，应返回 `status=ok`。

Actuator：`http://localhost:18080/actuator/health`

启用组件后（sample 默认已开启），冒烟脚本会自动验证：

- ping / 短信登录 JWT / 受保护接口
- 401、500、404 响应体 `traceId`
- `GET /api/dict/gender` 字典 API
- 请求日志中的 `traceId`、`userId`

手动抽查示例：

- `GET http://localhost:18080/api/sample/error/runtime` → 500 + 统一 JSON + `traceId`
- `GET http://localhost:18080/api/sample/auth/login` → 返回 `token`

### 按环境启动（test / prod / docker）

`component.*` 与 Spring Boot 一样，用 Profile 区分测试/正式，见建设指南 **§5.6**。

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
# 或 -Dspring-boot.run.profiles=prod
```

对应文件：`application-test.yml`、`application-prod.yml`。

## 4. 使用 Docker 中间件（可选）

**仅中间件（宿主机 mvn）** — 见 [deploy/README.md](../../deploy/README.md)：

```bash
cd deploy/docker/infra
cp .env.example .env
docker compose up -d
```

启动 sample 前设置 `SPRING_DATASOURCE_*` / `SPRING_DATA_REDIS_*` 为 `127.0.0.1`（详见 [docker.md](./docker.md) 方式二）。

**全栈容器** — 见 [docker.md](./docker.md) 方式一或 `./scripts/deploy-sample-docker.sh`。

## 4.1 Docker 部署 sample 全栈（可选）

```bash
./scripts/deploy-sample-docker.sh              # 默认 build + 冒烟
RUN_SMOKE=0 ./scripts/deploy-sample-docker.sh  # 仅常驻
```

容器内多阶段 Maven 编译 → MySQL + Redis + sample（`SPRING_PROFILES_ACTIVE=docker,test`）。详见 [docker.md](./docker.md)。

## 5. 业务项目引用（P1+ 模块发布后）

1. `dependencyManagement` 中 import `company-component-bom`。  
2. 添加所需 `common-*-spring-boot-starter` 依赖。  
3. 配置 `component.{feature}.enabled=true` 及必填项。

参见 [建设指南 · 第十四节](../../Spring Boot 可插拔积木组件建设指南.md)。
