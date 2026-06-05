# release · 组件库发布

> 本目录描述 **CCStarter 组件库** 的 Maven 发布与版本晋级，**不包含** sample 应用或业务系统的 K8s/生产部署。

## 发布物

| 制品 | 说明 |
|------|------|
| `company-component-bom` | 业务项目 `import` 统一版本 |
| `common-*-spring-boot-starter` | 对外接入依赖 |
| `common-*-autoconfigure` | 随 starter 传递，业务禁止直接依赖 |

当前版本策略见 [CHANGELOG.md](../../CHANGELOG.md) 与 [组件库实施进度 TODO.md](../../组件库实施进度%20TODO.md)。

## SNAPSHOT 发布（开发分支）

前置：`mvn clean verify` 通过（与 CI 一致）。

```bash
# 按团队私服 settings.xml 配置后执行（坐标占位 groupId: com.company.component）
mvn -B deploy -DskipTests=false
```

TODO 中「私服 RELEASE 发布」尚未执行时，可在本文件补充：

- 私服 URL、`settings.xml` 片段  
- SNAPSHOT / RELEASE 仓库 id  
- 发布审批与 tag 规则  

## RELEASE 晋级 checklist

- [ ] `mvn clean verify` + `./scripts/smoke-test.sh`（或 CI 绿）  
- [ ] 更新 [CHANGELOG.md](../../CHANGELOG.md) 版本节  
- [ ] 更新 [组件库实施进度 TODO.md](../../组件库实施进度%20TODO.md) 发布坐标  
- [ ] `${revision}` / `${auth.revision}` 与 `company-component-bom` 版本手动同步（见 [team-decisions.md](../../docs/architecture/team-decisions.md)）  
- [ ] `mvn deploy` 推 RELEASE（非 SNAPSHOT）  
- [ ] Git tag（如 `v1.0.0`）  

## 业务项目接入

发布完成后，业务项目 `dependencyManagement` import BOM，声明所需 starter。接入步骤见根 [README.md](../../README.md) 与 `docs/features/*/integration.md`。

## 占位说明

本目录随团队 Maven 流程完善后补充具体 `settings.xml`、流水线链接与 RELEASE 历史；**本地 Docker 联调**见 [../README.md](../README.md)。
