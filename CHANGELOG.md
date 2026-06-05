# Changelog

本文件记录组件库 **发布版本** 的行为与依赖变更。实施进度见 [组件库实施进度 TODO.md](./组件库实施进度 TODO.md)。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本遵循 [SemVer](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- **common-auth-login-redis**（**1.1.0-SNAPSHOT**）：`RedisSmsCodeStore` / `RedisEmailCodeStore`；`component.auth.login.redis.enabled` / `key-prefix`
- **common-auth-login-redis**：`common-auth-login-redis-autoconfigure` + `common-auth-login-redis-spring-boot-starter`；Testcontainers Redis 单测
- **sample**：Flyway 迁移 `V1__sys_dict.sql` + `JdbcDictDataProvider`（替代内存 `SampleDictSpiConfiguration`）
- **sample**：本地 H2 + Flyway；Docker MySQL + Redis 全栈（`deploy/sample/docker-compose.yml`）

### Changed

- **deploy/sample**：多阶段 Dockerfile（容器内 Maven 编译，无需宿主机 Maven / 预打包 JAR）；`.dockerignore` + BuildKit `.m2` 缓存
- **deploy-sample-docker.sh**：改为 `compose up --build`；`RUN_SMOKE=0` 可仅常驻不冒烟
- **common-dict**：`DictAutoConfiguration` 在 `RedisAutoConfiguration` 之后加载，修复 `cache.type=redis` 时 `DictController` 未注册
- **sample · docker**：`SPRING_PROFILES_ACTIVE=docker,test`（compose 激活，不再在 profile 文件内 `spring.profiles.include`）
- **deploy/sample**：compose 增加 MySQL / Redis healthcheck；支持 `MYSQL_PORT` / `SAMPLE_HOST_PORT` 环境变量
- **docs/guides/docker.md**：补充 sample 全栈部署说明

### Added（common-dict，前序未发布）

- **common-dict**：`GET /api/dict/{dictType}`（`component.dict.api.enabled`）、auth 白名单自动合并
- **common-dict**：`DictService`（`getItems` / `getLabel` / `getValue` / `refresh`）、`DictDataProvider` SPI
- **common-dict**：内存 / Redis 缓存（`component.dict.cache.type`）；sample 默认 memory，`docker` profile redis
- **common-dict**：`DictException` + `DICT_ENTRY_NOT_FOUND` / `DICT_LOAD_FAILED` 错误码映射

## [1.1.0-SNAPSHOT] - 2026-06-05

### Added

- **common-auth · login 编排**（MINOR）：`component.auth.login` 可配置短信 / 邮箱验证码登录与注册
- SMS / 邮箱独立 URL：发码、登录、注册；全局 `sms-length` / `email-code-length`（4 或 6）
- 测试验码：`test.sms`（`fixed-code` 与 `mobile-suffix` 二选一）、`test.email.fixed-code`；`allow-in-production` 生产防呆
- SPI：`LoginUserResolver`、`LoginUserRegistrar`、`SmsCodeSender`、`EmailCodeSender`、`SmsCodeStore`、`EmailCodeStore`
- 占位：`StubSmsCodeSender`、`StubEmailCodeSender`、内存 `SmsCodeStore` / `EmailCodeStore`（生产需替换）
- exception：`MappedHttpStatusException`、`LoginAuthException` 错误码映射
- 业务接入见 `docs/features/auth/integration.md` §3；设计见 `docs/features/auth/login-design.md`

> exception / log 模块版本仍为 **1.0.0-SNAPSHOT**；仅 auth 相关 artifact 升为 **1.1.0-SNAPSHOT**。

## [1.0.0-SNAPSHOT] - 2026-06-04

### Added

- **common-log**：TraceId 透传（`X-Trace-Id` → MDC `tid`）、请求 SLF4J 摘要、`@OperationLog` + SPI（**可发布** SNAPSHOT）
- **common-log**：业务接入见 `docs/features/log/integration.md`；MDC 约定见 `docs/architecture/logging.md`
- **common-exception**：`ApiErrorResponse.traceId`、`component.exception.include-trace-id`（读 MDC `tid`）
- **common-auth**：JWT 认证后 `userId` 写入 `Authentication#details` 供 log MDC
- **common-auth**：JWT + Spring Security（**可发布** SNAPSHOT；建议与 exception starter 一并引入）
- **common-auth**：`JwtAuthenticationFilter`、白名单、`JwtService`、SPI；业务接入见 `docs/features/auth/integration.md`
- **common-exception**：`AuthenticationException` / `AccessDeniedException` 映射；`ApiErrorResponseHttpWriter`
- **common-exception**：统一 API 异常处理（**可发布** SNAPSHOT）
- P1：`common-exception-autoconfigure` / `common-exception-spring-boot-starter`
- P0：父工程、BOM、sample、`docs/`、建设指南与实施 TODO

