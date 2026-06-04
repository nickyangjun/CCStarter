# common-auth-spring-boot-starter

对外接入入口，聚合 `common-auth-autoconfigure`（**无 Java 代码**）。**可发布** SNAPSHOT。

**必须与 `common-exception-spring-boot-starter` 一并引入**，401/403 才输出统一 JSON。

```xml
<dependency>
    <groupId>com.company.component</groupId>
    <artifactId>common-auth-spring-boot-starter</artifactId>
</dependency>
```

业务接入：[docs/features/auth/integration.md](../../docs/features/auth/integration.md)
