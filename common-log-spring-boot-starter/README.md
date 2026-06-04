# common-log-spring-boot-starter

对外接入入口，聚合 `common-log-autoconfigure`（**无 Java 代码**）。

```xml
<dependency>
    <groupId>com.company.component</groupId>
    <artifactId>common-log-spring-boot-starter</artifactId>
</dependency>
```

建议与 `common-exception-spring-boot-starter` 一并引入。业务接入：[docs/features/log/integration.md](../../docs/features/log/integration.md)；MDC 约定：[logging.md](../../docs/architecture/logging.md)。
