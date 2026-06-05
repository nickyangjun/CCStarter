# common-auth · 业务接入指南

> **common-auth** 当前 **1.1.0-SNAPSHOT**（含登录编排 MINOR）；exception / log 仍为 **1.0.0-SNAPSHOT**。  
> 鉴权与统一错误体**配套使用**：请同时引入 `common-exception-spring-boot-starter` 与 `common-auth-spring-boot-starter`；链路 `traceId` 建议再加 [common-log](../log/integration.md)。

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
        <!-- BOM 管理版本：1.1.0-SNAPSHOT（含 login 编排） -->
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
    # 禁止配置 login.test.*（测试验证码仅允许 test profile）
```

正式环境通过环境变量或配置中心注入 `JWT_SECRET`，勿在仓库提交明文密钥。

---

## 3. 登录编排（`component.auth.login`）

`login.enabled=true` 时，组件自动注册 **短信 / 邮箱** 发码、登录、注册 Controller，并在 Security 白名单中合并对应 `paths.*`（建议业务仍显式写出，便于排查 401）。

详细设计见 [login-design.md](./login-design.md)。

### 3.1 开启登录编排

```yaml
component:
  auth:
    enabled: true
    jwt-secret: ${JWT_SECRET}
    whitelist:
      - /actuator/**
      - /api/auth/sms/**
      - /api/auth/email/**
    login:
      enabled: true
      sms-length: 6              # 仅允许 4 或 6
      email-code-length: 6
      sms:
        enabled: true
        code-ttl-seconds: 300
        resend-interval-seconds: 60
      email:
        enabled: true
      register:
        enabled: true              # 独立注册接口
        login-as-register: true    # 登录时用户不存在则自动注册
        issue-token-on-register: true
```

| 配置键 | 说明 |
|--------|------|
| `login.enabled` | 默认 `false`；为 `true` 时要求 `auth.enabled=true` |
| `login.sms.enabled` / `login.email.enabled` | 启用对应通道的 Controller |
| `login.register.enabled` | 注册 `POST .../register` |
| `login.register.login-as-register` | 登录接口在用户不存在时调用 `LoginUserRegistrar` |
| `*.paths.*` | 各接口 URL，默认见下表 |

### 3.2 HTTP 接口（默认路径）

| 能力 | 默认路径 | 请求体 | 响应 |
|------|----------|--------|------|
| 发送短信验证码 | `POST /api/auth/sms/send-code` | `{"mobile":"13800138000"}` | `{success, ttlSeconds, resendAfterSeconds}` |
| 短信验证码登录 | `POST /api/auth/sms/login` | `{"mobile":"...","code":"123456"}` | `{accessToken, userId, username, expireMinutes}` |
| 短信注册 | `POST /api/auth/sms/register` | 同上 | 同上或仅 `{userId, username}`（`issue-token-on-register=false`） |
| 发送邮箱验证码 | `POST /api/auth/email/send-code` | `{"email":"u@example.com"}` | 同短信发码 |
| 邮箱验证码登录 | `POST /api/auth/email/login` | `{"email":"...","code":"..."}` | 同短信登录 |
| 邮箱注册 | `POST /api/auth/email/register` | 同上 | 同短信注册 |

**注意**：发码响应**禁止**回显验证码；客户端凭短信/邮件获取。

登录成功后访问受保护接口：

```http
GET /api/orders HTTP/1.1
Authorization: Bearer <accessToken>
```

样例：`company-component-samples/sample-boot-app` 中 `SampleLoginSpiConfiguration`、`application.yml`。

### 3.3 业务 SPI（必填 / 按需）

| SPI | 何时需要 | 职责 |
|-----|----------|------|
| `LoginUserResolver` | `login.enabled=true` **必填** | `findByMobile` / `findByEmail` 查用户 |
| `LoginUserRegistrar` | `register.enabled` 或 `login-as-register=true` **必填** | `register(RegisterRequest)` 创建用户并返回 `LoginPrincipal` |
| `SmsCodeSender` | 生产发真实短信 | 调用云厂商 SDK 发送验证码 |
| `EmailCodeSender` | 生产发真实邮件 | SMTP 或云邮件 API |
| `SmsCodeStore` | 生产多实例部署 **建议** | 验证码持久化（默认内存，仅 dev/sample） |
| `EmailCodeStore` | 同上 | 邮箱通道独立 Store |

实现类注册为 Spring Bean 即可；组件通过 `@ConditionalOnMissingBean` 优先使用业务实现。

```java
@Configuration
public class MyLoginSpiConfiguration {

    @Bean
    LoginUserResolver loginUserResolver(UserRepository users) {
        return new LoginUserResolver() {
            @Override
            public Optional<LoginPrincipal> findByMobile(String mobile) {
                return users.findByMobile(mobile).map(u -> new LoginPrincipal(u.getUsername(), u.getId()));
            }
            @Override
            public Optional<LoginPrincipal> findByEmail(String email) {
                return users.findByEmail(email).map(u -> new LoginPrincipal(u.getUsername(), u.getId()));
            }
        };
    }

    @Bean
    LoginUserRegistrar loginUserRegistrar(UserService userService) {
        return request -> userService.register(request);
    }
}
```

生产环境还需提供 `SmsCodeSender` / `EmailCodeSender` 及 Redis `SmsCodeStore` / `EmailCodeStore`（见 [login-design.md](./login-design.md) §6、根目录 TODO §4.3）。

### 3.4 测试环境验证码（仅 `test` profile）

测试行为**不**读取 `spring.profiles.active`，仅由 `component.auth.login.test.*` 控制；配置写在 `application-test.yml`，**禁止**写入默认 `application.yml` 或 `application-prod.yml`。

```yaml
# application-test.yml
component:
  auth:
    login:
      test:
        allow-in-production: true   # test.sms/email 任一 enabled 时必填，生产防呆确认
        sms:
          enabled: true
          fixed-code: "123456"      # 与 mobile-suffix 二选一
        email:
          enabled: true
          fixed-code: "123456"
```

| 模式 | 配置 | 规则 |
|------|------|------|
| 短信固定码 | `test.sms.fixed-code` | 任意手机号，提交码等于固定码即通过 |
| 短信尾号 | `test.sms.mobile-suffix: true` | 提交码等于手机号后 `sms-length` 位（与 `fixed-code` 互斥） |
| 邮箱固定码 | `test.email.fixed-code` | 任意邮箱，提交码等于固定码即通过 |

本地脚本默认激活 test profile：`./scripts/run-sample.sh`、`./scripts/smoke-test.sh`。Docker 联调使用 `docker` profile（`application-docker.yml` 内 `spring.profiles.include: test`）。

### 3.5 登录相关错误码

| 场景 | HTTP | `code` |
|------|------|--------|
| 验证码错误 / 过期 | 400 | `INVALID_SMS_CODE` / `INVALID_EMAIL_CODE` |
| 发码过于频繁 | 429 | `SEND_TOO_FREQUENT` |
| 用户不存在（未开 login-as-register） | 404 | `USER_NOT_FOUND` |
| 手机号已注册 | 409 | `MOBILE_ALREADY_REGISTERED` |
| 无 Token / Token 无效 | 401 | `UNAUTHORIZED` |

响应体字段与 `common-exception` 一致（`code`、`message`、`timestamp`、`path`、`traceId` 等）。

---

## 4. 自定义登录（不使用 login 编排）

若仅需密码登录等自定义方式，可关闭 `login.enabled`，在业务 Controller 中注入 `JwtService` 签发 Token：

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

样例参考：`DemoAuthController`、`SecureHelloController`。

---

## 5. 错误响应（与 exception 一致）

| 场景 | HTTP | `code` |
|------|------|--------|
| 无 Token / Token 无效 | 401 | `UNAUTHORIZED` |
| 已认证但无权限 | 403 | `FORBIDDEN` |

响应体字段与 `common-exception` 一致（`code`、`message`、`timestamp`、`path` 等）。

---

## 6. 可选 SPI（JWT 层）

| SPI | 用途 |
|-----|------|
| `JwtClaimsCustomizer` | 签发前向 JWT 追加自定义 Claims |
| `AuthUserDetailsLoader` | 解析 Token 后按用户名加载 `UserDetails`（可选） |

实现类注册为 Spring Bean 即可；无实现不影响启动。

---

## 7. 关闭鉴权

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

## 8. 验证清单

- [ ] `mvn clean verify` 通过（业务项目或引用 sample）
- [ ] 白名单路径无 Token 可访问
- [ ] 受保护路径无 Token 返回 401 + 统一 JSON
- [ ] 合法 Token 可访问受保护路径
- [ ] prod 使用 `${JWT_SECRET}`，无密钥入库
- [ ] 使用 login 编排时：已实现 `LoginUserResolver`（及按需 `LoginUserRegistrar`）
- [ ] prod **未**配置 `login.test.*`；生产已提供真实 `SmsCodeSender` / `EmailCodeSender` 与 Redis Store

---

## 相关文档

- [能力概述](./README.md)
- [详细设计](./design.md)
- [登录编排设计](./login-design.md)
- [exception 接入](../exception/README.md)
- [样例应用](../../../company-component-samples/sample-boot-app/)
