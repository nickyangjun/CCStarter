# Changelog

本文件记录组件库 **发布版本** 的行为与依赖变更。实施进度见 [组件库实施进度 TODO.md](./组件库实施进度 TODO.md)。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本遵循 [SemVer](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- P2：**common-auth** Phase 3 — JWT（jjwt 0.12.6）、`JwtAuthenticationFilter`、`SecurityFilterChain`、白名单、`AuthAutoConfiguration` 条件装配
- P2：与 **common-exception** 联调 401/403 统一 JSON（`ApiErrorResponseHttpWriter`、Security 异常映射）
- P2：sample `AuthIntegrationTest`；`AuthAutoConfigurationTests` / `JwtServiceTest`
- P2：`common-auth` 阶段 0 文档与 Maven 模块骨架
- CI：GitHub Actions `mvn verify`

## [1.0.0-SNAPSHOT] - 2026-06-04

### Added

- **common-exception**：统一 API 异常处理（**可发布** SNAPSHOT）
- P1：`common-exception-autoconfigure` / `common-exception-spring-boot-starter`
- P0：父工程、BOM、sample、`docs/`、建设指南与实施 TODO

