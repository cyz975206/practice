# CLAUDE.md — 印章管理系统（seal_management）

> 本文件是给 Claude 的项目共享上下文，每次会话自动加载。两台开发设备经 git 同步本文件，保证 Claude 行为一致。
> 权威设计文档：`CONTEXT.md`（术语表）、`docs/adr/*`（9 份 ADR）、`docs/backend-architecture.md`（架构 + 顶部"实现状态"表）。

## 这是什么

面向大型央国企/国企集团的**多法人实体**印章管理平台（实体印章 + 电子印章全生命周期、用印申请与审批、智能设备集成、电子签章对接、审计统计）。同时是**练习/简历项目**——选型偏向可识别、高频的企业 Java 栈，并保持信创可移植。

## ⚠️ 仓库边界（重要）

本仓库 `practice/` 下有多个**互不相关**的练习项目：`seal_management`（本后端）、`sys`、`sys_vue`。它们**技术栈不同、互不依赖**。当前工作只在 `seal_management/` 内，**不要把 sys/sys_vue 的代码、依赖、约定混进来**。git 仓库根在 `practice/`（commit 路径前缀为 `seal_management/...`）。

## 技术栈（锚点；版本钉死见 pom.xml + 架构文档 §2）

- Java **17** + Spring Boot **3.5.16**（⚠️ 3.5 已 EOL，换取文档/生态识别度）
- PostgreSQL **16** + **MyBatis-Plus 3.5.16**（多租户插件 = 行级隔离）
- Redis 7 + Redisson（缓存/分布式锁）· Flowable 7.1（审批引擎，已接线未用）
- Spring Security 6 + jjwt 0.12.6 · Flyway（DB 迁移）· Knife4j（API 文档）· Druid（连接池）
- **待接线**（pom 注释，按切片懒加载）：RabbitMQ / XXL-JOB / Elasticsearch / MinIO

## 构建 / 测试 / 运行

```bash
mvn test-compile        # 只编译（主源 + 测试源）
mvn test                # 全量集成测试（@SpringBootTest，需 PG+Redis）
mvn spring-boot:run     # 本地跑（dev profile）
```

- dev 中间件在局域网 VM `192.168.1.128`（PG:5432 / Redis:6380，见 `application-dev.yml`、`docker-compose.yml`）。两台开发设备同局域网，clone 即跑。
- 测试种子：启动时 `IamDataInitializer` 幂等建默认法人实体（集团本部）+ 系统角色 + `admin/888888`。

## 架构要点

- **领域上下文 + 六边形分层**：包 `com.cyz.seal.<context>`，每上下文内 `domain / application / infrastructure / interfaces`。已建：`common` / `org` / `iam`；设计未建：`seal` / `usage` / `approval` / `integration` / `notification` / `audit` / `search` / `scheduler` / `storage`（见架构文档"实现状态"表）。
- **多租户行级隔离（ADR-0002）**：每张业务表带 `legal_entity_id`，`TenantLineInnerInterceptor` 自动注入过滤。租户表实体**不映射** `legal_entity_id`（拦截器注入）；全局表（`legal_entity`）在 `MybatisPlusConfig.GLOBAL_TABLES`。请求级上下文 `LegalEntityContext`（ThreadLocal，由 JWT 过滤器填充）。
- **认证授权**：Spring Security + JWT（无状态）。`@EnableMethodSecurity` + 控制器类级 `@PreAuthorize`。角色 = **4 个系统角色**：超级管理员 / 超级审计员（GROUP，绕过多租户——**待实现**）+ 系统管理员 / 普通用户（ENTITY）。详见 `CONTEXT.md` → Roles。
- **数据约定**：id = MyBatis-Plus 雪花（ASSIGN_ID）；审计字段（create/update time/by）由 `MyMetaObjectHandler` 填充；逻辑删除 `deleted`；统一响应 `Result` + `BusinessException`。`@MapperScan("com.cyz.seal.**.infrastructure.persistence.mapper")` 自动注册 Mapper。
- **迁移**：Flyway 前进式（`db/migration/V*n__*.sql`）。**已应用的 V\* 不可改**（checksum 会致启动失败），要改就新加一个 V。

## 工作约定

- 写代码沿用 `org` / `iam` 既有切片的模式（实体 / Service / Controller / DTO / 迁移 / 测试）。
- 设计决策进 ADR（`docs/adr/`）；术语进 `CONTEXT.md`（纯术语表，不放实现细节）。
- 提交信息：`<type>(seal_management): <中文摘要>`，type ∈ feat / fix / docs / chore。
