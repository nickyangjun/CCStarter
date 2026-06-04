# Changelog

本文件记录组件库 **发布版本** 的行为与依赖变更。实施进度见 [组件库实施进度 TODO.md](./组件库实施进度 TODO.md)。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本遵循 [SemVer](https://semver.org/lang/zh-CN/)。

## [Unreleased]

（无）

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

