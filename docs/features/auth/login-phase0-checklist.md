# common-auth · 登录编排 · 阶段 0 检查清单

> 设计文档：[login-design.md](./login-design.md)

## 附录 A · 前缀登记

- [x] `component.auth.login` 已写入 [config-prefix-registry.md](../../architecture/config-prefix-registry.md)（子前缀，归属 auth 域）

## 阶段 0 文档

- [x] [login-design.md](./login-design.md)
- [x] 独立 URL、SMS 首期、`sms-length`(4/6)、测试 `fixed-code`∥`mobile-suffix` 二选一、注册与登录即注册 已记录
- [x] SPI 草案（`SmsCodeSender`、`LoginUserResolver`、`LoginUserRegistrar` 等）
- [x] 与 P1 JWT / 白名单 / exception 协作已说明

## 附录 B · 能力边界自检

| # | 问题 | 结论 |
|---|------|------|
| 1 | 3 个以上项目可复用？ | **是**（标准短信登录 + 可选注册） |
| 2 | 不依赖业务表？ | **是**（用户/发码走 SPI） |
| 3 | `login.enabled=false` 无残留？ | **是**（不注册 login Controller） |
| 4 | 测试码不会默认进生产？ | **是**（`test.enabled` 默认 false + 生产防呆校验） |
| 5 | 敏感信息不落日志？ | **是**（design §9） |
| 6 | 注册也在本模块？ | **是**（可配置 `register.*`） |
| 7 | 流程图？ | **是**（login-design §4） |
| 8 | 单测场景已规划？ | **是**（login-design §11） |

## 附录 C · 编码前总检查

- [x] 附录 A 已登记
- [x] login-design 已提交
- [x] P1 auth 已发布，login 为 MINOR 增量
- [x] 团队决策（独立 URL、SMS、测试规则、注册）已写入 design §12
- [x] 实现已按设计编码（P2.1）

**阶段 0 结论**：✅ 已进入实现（登录编排 MINOR）

**待办**：`SmsCodeSender` 第三方 SDK、`SmsCodeStore` Redis — 见根目录 `组件库实施进度 TODO.md` §4.3。
