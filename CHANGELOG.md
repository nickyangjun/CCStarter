# Changelog

本文件记录组件库 **发布版本** 的行为与依赖变更。实施进度见 [组件库实施进度 TODO.md](./组件库实施进度 TODO.md)。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本遵循 [SemVer](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

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

