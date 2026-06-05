# common-dict · 业务接入指南

> **1.0.0-SNAPSHOT** 可发布。建议与 **common-exception** 一并引入；若接口走 HTTP 且需匿名读字典，再加 **common-auth** 做白名单（组件可自动合并 `api` 路径）。

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
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-exception-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-dict-spring-boot-starter</artifactId>
    </dependency>
    <!-- HTTP API + 多实例 Redis 缓存 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!-- 若启用 JWT，字典公开读接口需 auth 白名单（见 §4） -->
    <dependency>
        <groupId>com.company.component</groupId>
        <artifactId>common-auth-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

本地开发：组件库根目录 `mvn clean install`。

---

## 2. 数据库表规范

全团队统一 **`sys_dict_type` / `sys_dict_item`**（字段、命名分层见 [design.md §4](./design.md#4-统一字典表规范)）。

组件**不包含** DDL 迁移与 CRUD；业务建表并实现 **`DictDataProvider`** 从库加载。

---

## 3. 配置

### 3.1 最小配置（仅 `DictService` Bean）

```yaml
component:
  dict:
    enabled: true
    cache:
      type: memory    # 本地 dev；生产见 §3.2
```

### 3.2 生产 / Docker（Redis + HTTP API）

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6379

component:
  dict:
    enabled: true
    api:
      enabled: true              # 注册 GET /api/dict/{dictType}
      base-path: /api/dict       # 可改，须以 / 开头
    cache:
      type: redis
      ttl-seconds: 3600
      key-prefix: "${spring.application.name:app}:dict"
      null-ttl-seconds: 60
```

| 配置键 | 默认 | 说明 |
|--------|------|------|
| `enabled` | `false` | 总开关 |
| `api.enabled` | `false` | `true` 时注册 `DictController` |
| `api.base-path` | `/api/dict` | 列表接口前缀 |
| `cache.type` | `memory` | `memory` \| `redis` |
| `cache.ttl-seconds` | `3600` | 缓存 TTL（秒） |
| `cache.key-prefix` | `app:dict` | Redis key 命名空间 |
| `cache.null-ttl-seconds` | `60` | 空列表短 TTL，防穿透 |

### 3.3 sample 约定

| Profile | `cache.type` | `api` |
|---------|--------------|-------|
| 默认 | `memory` | `enabled: true` |
| `docker` | `redis` | 同上 |

---

## 4. 实现 `DictDataProvider`（必填）

`component.dict.enabled=true` 时**必须**注册 Bean：

```java
@Configuration
public class DictSpiConfiguration {

    @Bean
    DictDataProvider dictDataProvider(SysDictMapper mapper) {
        return dictType -> mapper.selectEnabledItems(dictType).stream()
                .map(row -> DictItem.of(
                        row.getDictType(),
                        row.getItemCode(),
                        row.getItemLabel(),
                        row.getItemValue(),   // NULL 时组件回退为 code
                        row.getSortOrder(),
                        row.getCssClass(),
                        parseExtra(row.getExtraJson())))
                .toList();
    }
}
```

表列 `snake_case` → `DictItem` 字段 `camelCase`，映射在 SPI 内完成（见 design §4.0）。

---

## 5. 使用 `DictService`（服务端）

```java
@Service
public class OrderExportService {

    private final DictService dictService;

    public String exportRow(String statusCode) {
        String label = dictService.requireLabel("order_status", statusCode);
        return label;
    }

    public void afterAdminUpdate(String dictType) {
        dictService.refresh(dictType);   // 管理端改库后调用
    }
}
```

| 方法 | 说明 |
|------|------|
| `getItems(dictType)` | 列表（走缓存） |
| `getLabel` / `getValue` | `Optional` 查询 |
| `requireLabel` / `requireValue` | 缺失抛 `DictException`（`DICT_ENTRY_NOT_FOUND`） |
| `refresh` / `refreshAll` | 失效缓存 |

---

## 6. HTTP API（`api.enabled=true`）

### 6.1 查询字典项

```http
GET /api/dict/{dictType} HTTP/1.1
X-Trace-Id: <optional>
```

响应示例：

```json
{
  "dictType": "gender",
  "items": [
    {
      "code": "1",
      "label": "男",
      "value": "M",
      "sortOrder": 1,
      "cssClass": null,
      "extra": null
    }
  ]
}
```

- 未知 `dictType`：返回 **200** + `items: []`（由 `DictDataProvider` 决定）。
- **不提供** 管理端增删改接口（业务自建后台）。

### 6.2 与 common-auth 白名单

引入 **auth** 且字典接口需匿名读取时：

1. 开启 `component.dict.api.enabled=true` 后，auth 会**自动合并** `{base-path}/**` 到白名单（需 classpath 同时存在 dict + auth）。
2. 建议在 `component.auth.whitelist` **显式写出** `/api/dict/**`，便于排查 401。

管理端 `refresh` 应放在**受保护**业务接口内，不要暴露为组件公开 API。

---

## 7. 错误码（与 exception 一致）

| 场景 | HTTP | `code` |
|------|------|--------|
| `requireLabel` / `requireValue` 无匹配 | 404 | `DICT_ENTRY_NOT_FOUND` |
| `DictDataProvider` 加载失败 | 502 | `DICT_LOAD_FAILED` |
| 非法 `dictType` 参数 | 400 | `DICT_TYPE_INVALID` |

---

## 8. 验证清单

- [ ] `mvn clean verify` 通过
- [ ] 已实现 `DictDataProvider`，`sys_dict_*` 表符合 design §4
- [ ] 生产 `cache.type=redis` + `spring.data.redis.*` 可连通
- [ ] `GET /api/dict/{type}` 无 Token 可访问（若启用 API + auth）
- [ ] 管理端改字典后调用 `dictService.refresh(dictType)`
- [ ] Redis key 使用独立前缀，不与 login / 业务缓存混用

---

## 相关文档

- [能力概述](./README.md)
- [详细设计](./design.md)
- [exception 接入](../exception/README.md)
- [auth 接入](../auth/integration.md)
- [样例应用](../../../company-component-samples/sample-boot-app/)
