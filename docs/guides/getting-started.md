# 快速开始

## 1. 克隆与 JDK

```bash
git clone <repository-url>
cd CCStarter
```

- JDK **17+**：`java -version`
- Maven **3.9+**：`mvn -version`

## 2. 全量构建

在仓库根目录：

```bash
mvn clean verify
mvn clean install
```

## 3. 运行样例应用

```bash
cd company-component-samples/sample-boot-app
mvn spring-boot:run
```

验证：访问 `http://localhost:18080/api/sample/ping`，应返回 `status=ok`。

Actuator：`http://localhost:18080/actuator/health`

启用 `common-exception` 后（sample 默认已开启），可验证：

- `GET http://localhost:18080/api/sample/error/runtime` → 500 + 统一 JSON
- `GET http://localhost:18080/api/sample/error/missing-param` → 400
- `GET http://localhost:18080/api/sample/not-exists` → 404

### 按环境启动（test / prod / docker）

`component.*` 与 Spring Boot 一样，用 Profile 区分测试/正式，见建设指南 **§5.6**。

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
# 或 -Dspring-boot.run.profiles=prod
```

对应文件：`application-test.yml`、`application-prod.yml`。

## 4. 使用 Docker 中间件（可选）

```bash
cd deploy/docker
cp .env.example .env   # 按需修改
docker compose up -d
cd ../../company-component-samples/sample-boot-app
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

详见 [docker.md](./docker.md)。

## 5. 业务项目引用（P1+ 模块发布后）

1. `dependencyManagement` 中 import `company-component-bom`。  
2. 添加所需 `common-*-spring-boot-starter` 依赖。  
3. 配置 `component.{feature}.enabled=true` 及必填项。

参见 [建设指南 · 第十四节](../../Spring Boot 可插拔积木组件建设指南.md)。
