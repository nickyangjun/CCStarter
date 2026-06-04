# common-exception-autoconfigure

统一 API 异常响应的自动配置实现模块。

| 项 | 值 |
|----|-----|
| 配置前缀 | `component.exception` |
| 设计文档 | [docs/features/exception/](../../docs/features/exception/) |

## 依赖

业务项目请使用 **`common-exception-spring-boot-starter`**，不要直接依赖本模块。

## 配置

```yaml
component:
  exception:
    enabled: true
    include-path: true
    expose-stack-trace: false
    default-error-code: INTERNAL_ERROR
```

## 当前进度

- ✅ 阶段 0 设计文档
- ✅ `ComponentGlobalExceptionHandler` + `ExceptionMappingSupport`
- ✅ SPI `ExceptionErrorCodeResolver`、条件装配与映射单测
- ✅ sample 集成测试（`ExceptionIntegrationTest`）
