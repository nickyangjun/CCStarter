# common-auth · 业务接入指南

> 一期 SNAPSHOT 可发布。鉴权与统一错误体**配套使用**：请同时引入 `common-exception-spring-boot-starter` 与 `common-auth-spring-boot-starter`；链路 `traceId` 建议再加 [common-log](../log/integration.md)。

---

## 1. Maven 依赖

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
    <!-- 统一 API 错误 JSON（auth 401/403 依赖此模块输出） -->
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-exception-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-auth-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

本地开发前在组件库根目录执行：`mvn clean install`。

---

## 2. 配置

### 2.1 最小配置（`application.yml`）

```yaml
component:
  exception:
    enabled: true
    include-path: true
    expose-stack-trace: false
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}   # 必填，建议 ≥ 32 字符；禁止写死在 prod
    expire-minutes: 120
    whitelist:
      - /actuator/**
      - /api/public/**
      - /api/auth/login          # 登录接口示例，按业务实际路径配置
```

| 配置键 | 说明 |
|--------|------|
| `component.auth.enabled` | `false`（默认）时不注册组件 Security 链与 JWT 过滤器 |
| `jwt-secret` | `enabled=true` 时启动校验非空 |
| `whitelist` | Ant 路径；组件默认合并 `/error`、`/actuator/health` |
| `header-name` / `token-prefix` | 默认 `Authorization` + `Bearer ` |

### 2.2 多环境

```yaml
# application-prod.yml
component:
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}
```

正式环境通过环境变量或配置中心注入 `JWT_SECRET`，勿在仓库提交明文密钥。

---

## 3. 登录与受保护接口

组件**不提供**登录 Controller，业务在登录接口中注入 `JwtService` 签发 Token：

```java
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        // 1. 校验用户名密码（业务自己的 UserService）
        // 2. 签发 JWT
        String token = jwtService.createToken(req.getUsername(), Map.of("userId", userId));
        return new TokenResponse(token);
    }
}
```

客户端访问受保护接口：

```http
GET /api/orders HTTP/1.1
Authorization: Bearer <token>
```

样例参考：`company-component-samples/sample-boot-app` 中 `DemoAuthController`、`SecureHelloController`。

---

## 4. 错误响应（与 exception 一致）

| 场景 | HTTP | `code` |
|------|------|--------|
| 无 Token / Token 无效 | 401 | `UNAUTHORIZED` |
| 已认证但无权限 | 403 | `FORBIDDEN` |

响应体字段与 `common-exception` 一致（`code`、`message`、`timestamp`、`path` 等）。

---

## 5. 可选 SPI

| SPI | 用途 |
|-----|------|
| `JwtClaimsCustomizer` | 签发前向 JWT 追加自定义 Claims |
| `AuthUserDetailsLoader` | 解析 Token 后按用户名加载 `UserDetails`（可选） |

实现类注册为 Spring Bean 即可；无实现不影响启动。

---

## 6. 关闭鉴权

仅需要统一异常、不需要 JWT 时：

```yaml
component:
  exception:
    enabled: true
  auth:
    enabled: false
```

`enabled=false` 时不注册本组件的 `SecurityFilterChain`。若 classpath 仍有 `spring-boot-starter-security` 且业务无其他 Security 配置，可能触发 Spring Boot 默认 Security（见 [design.md](./design.md) §3.1）。

---

## 7. 验证清单

- [ ] `mvn clean verify` 通过（业务项目或引用 sample）
- [ ] 白名单路径无 Token 可访问
- [ ] 受保护路径无 Token 返回 401 + 统一 JSON
- [ ] 合法 Token 可访问受保护路径
- [ ] prod 使用 `${JWT_SECRET}`，无密钥入库

---

## 相关文档

- [能力概述](./README.md)
- [详细设计](./design.md)
- [exception 接入](../exception/README.md)
- [样例应用](../../../company-component-samples/sample-boot-app/)
