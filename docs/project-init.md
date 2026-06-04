# 项目初始化分析报告

## 结论摘要

本项目是 Java 8 + Maven 多模块 Cloud ERP 后端项目。统一入口为 `erp-gateway`，业务能力按领域拆分到 `erp-server-*`，模型放在 `erp-model-*`，跨服务契约放在 `erp-rpc-*`，第三方平台能力放在 `erp-sdk-*`，公共能力放在 `erp-common-*`。

当前仓库未发现前端工程配置，因此前端组件体系为“当前仓库未确认/不适用”。本文档中的前端相关内容只记录事实和建议，不编造不存在的前端规范。

## 证据来源

| 结论类型 | 来源 |
| --- | --- |
| Maven 多模块、Java 8、Spring Boot/Spring Cloud 版本 | 根 `pom.xml`、各模块 `pom.xml` |
| Gateway 统一入口 | `erp-gateway/pom.xml`、`erp-gateway/src/main/java/com/cloud/erp/gateway` |
| Nacos/Seata 配置 | `erp-server/*/src/main/resources/bootstrap*.yml` |
| Controller/Service/Mapper 分层 | `erp-server/erp-server-sys/src/main/java/com/erp/server/sys` |
| `ApiResult` 和 `BaseController` | `erp-common/erp-common-core/src/main/java/com/common/core/controller/BaseController.java` |
| 全局异常处理 | `erp-common/erp-common-business/src/main/java/com/common/business/config/GlobalExceptionHandler.java` |
| Feign/RPC 契约 | `erp-rpc/erp-rpc-sys/src/main/java/com/erp/rpc/sys/feign` |
| Entity/DTO/VO 目录 | `erp-model/erp-model-sys/src/main/java/com/erp/model/sys` |
| MyBatis XML | `erp-server/*/src/main/resources/mapper/*.xml` |
| 用户确认规范 | 用户补充：Java8 + Nacos + Seata + RocketMQ + pgsql + XXL-JOB + Redis + Lombok + PostgreSQL + MyBatis/MyBatis-Plus + Maven + Spring Boot + Spring Cloud；Gateway 统一入口；`erp-generator` 模板；Entity/DTO/VO 语义 |

## 1. 技术栈

### 代码确认

| 类别 | 技术 | 来源 |
| --- | --- | --- |
| 语言 | Java 8 | 根 `pom.xml`、模块 `pom.xml` |
| 构建 | Maven 多模块 | 根 `pom.xml` |
| 后端框架 | Spring Boot、Spring Cloud | 根 `pom.xml`、`erp-server/pom.xml` |
| 网关 | Spring Cloud Gateway | `erp-gateway/pom.xml` |
| 服务注册/配置 | Nacos | `erp-common-business/pom.xml`、`bootstrap*.yml` |
| 分布式事务 | Seata | 根 `pom.xml`、`erp-server/pom.xml`、`bootstrap.yml` |
| 缓存/锁 | Redis、Redisson | `erp-common-business/pom.xml` |
| 消息 | RocketMQ | `erp-common/pom.xml`、`erp-common-message` |
| 调度 | XXL-JOB | `erp-server/erp-server-sys/pom.xml`、`erp-chrome/pom.xml` |
| 数据库 | PostgreSQL/pgsql、MySQL 驱动 | 根 `pom.xml`、`erp-common-business/pom.xml` |
| ORM | MyBatis、MyBatis-Plus | 根 `pom.xml`、Mapper/Service 代码 |
| 工具库 | Lombok、FastJSON/FastJSON2、MapStruct、EasyExcel、Hutool、OkHttp | 相关 `pom.xml` |
| 测试 | Spring Boot Test | `erp-server/pom.xml`、测试目录 |

### 用户确认

- 统一入口网关为 Gateway。
- 使用 `erp-generator` 模板生成初始化代码。
- `Entity` 表示数据层，`DTO` 表示入参，`VO` 表示出参，系统间传递使用 `VO`。

### 待确认

- 统一 CI 流程。
- 统一 Lint/Format 工具。
- 全量测试策略。
- 提交前强制检查命令。

## 2. 项目结构

| 目录/模块 | 职责 | 状态 |
| --- | --- | --- |
| `erp-gateway` | 统一入口网关，包含鉴权、签名、过滤、路由、上下文、异常处理 | 代码确认 |
| `erp-common-core` | 基础响应、异常、工具、校验、核心注解 | 代码确认 |
| `erp-common-business` | Feign、Redis、全局异常、权限、幂等、数据权限、公共业务配置 | 代码确认 |
| `erp-common-message` | 消息公共能力 | 代码确认 |
| `erp-model-*` | 业务域模型，常见子目录为 `entity`、`dto`、`vo`、`enums` | 代码确认 |
| `erp-rpc-*` | 业务域 Feign/RPC 接口 | 代码确认 |
| `erp-server-*` | 业务服务实现 | 代码确认 |
| `erp-sdk-*` | 第三方平台 SDK 和外部系统适配 | 代码确认 |
| `erp-generator` | 初始化代码生成模板 | 用户确认 + 目录确认 |
| `erp-chrome` | Spring Boot 插件服务 | 代码确认 |
| `docs` | 项目文档 | 代码确认 |

模块边界规则：

- 公共能力放 `erp-common`。
- 业务模型放 `erp-model`。
- 跨服务契约放 `erp-rpc`。
- 业务实现放 `erp-server`。
- 第三方平台能力放 `erp-sdk`。
- 初始化代码优先参考 `erp-generator`。

## 3. 前端组件体系

### 代码确认

当前仓库未发现：

- `package.json`
- 前端包管理锁文件
- Vite/Webpack/Next/Vue/React 配置
- 前端组件目录

### 结论

前端组件体系在当前仓库中不适用。不得编造前端框架、UI 库、路由、状态管理、表单、请求封装、主题、样式和 props 规范。

### 建议规范

未来若引入前端工程，应先确认实际前端根目录、框架、包管理器、构建工具和目录结构，再补充前端规则。

## 4. 后端 / 服务架构

### API 分层

典型结构：

- `controller/api`：Web/API 控制器。
- `controller/feign`：服务间调用控制器。
- `controller/app`、`controller/pda`：特定客户端控制器。
- `service`：服务接口。
- `service/impl`：服务实现。
- `mapper`：MyBatis/MyBatis-Plus Mapper 接口。
- `resources/mapper/*.xml`：MyBatis XML SQL。

### 统一响应与异常

- Controller 通常继承 `BaseController`。
- API 返回 `ApiResult`。
- `GlobalExceptionHandler` 统一处理业务异常、Feign 异常、参数校验异常、数据库异常、运行时异常。
- 业务异常使用 `ServiceException`。
- Feign 异常使用 `FeignServiceException`。

### 鉴权、权限、日志和幂等

代码中存在以下公共机制，应优先复用：

- 网关鉴权与过滤：`AuthGatewayFilter`、`SignatureVerificationFilter`、`SqlInjectionFilter`、`XssInjectionFilter`。
- 操作日志注解：`@LogSystemModule`、`@LogAction`。
- 权限/数据权限注解：`@RequestPermissions`、`@DataPermission`、`@MenuCode`。
- 幂等/锁相关注解：`@Idempotent`、`@DataIdempotent`、`@DistributeLocker`。
- Feign 配置：`FeignInterceptor`、`FeignErrorDecoder`、`FeignTimeoutConfig`。

### 数据访问

- `Entity` 表示数据层对象。
- Mapper 继承 `BaseMapper<Entity>`。
- Service 继承 `SuperService<Entity>`。
- ServiceImpl 继承 `SuperServiceImpl<Mapper, Entity>`。
- 简单查询优先使用 MyBatis-Plus wrapper。
- 复杂分页和动态 SQL 放入 Mapper XML。

## 5. 项目规范

### 代码确认

- Java 类名使用 PascalCase。
- Controller 以 `Controller` 结尾。
- Service 接口以 `Service` 结尾。
- Service 实现以 `ServiceImpl` 结尾。
- Mapper 接口以 `Mapper` 结尾。
- Mapper XML 与 Mapper 接口对应。
- 模型类常用 Lombok。
- 配置使用 `bootstrap.yml`、`bootstrap-{env}.yml`。
- 日志配置使用 `logback-spring.xml`。

### 用户确认

- `Entity` 表示数据层。
- `DTO` 表示入参。
- `VO` 表示出参。
- 系统间传递使用 `VO`。

### 兼容说明

历史代码中存在部分 DTO 用作返回对象。新增代码按用户确认语义执行；修改旧接口时不强制重构，优先保持兼容。

## 6. 黄金实践

- 新增 API 前先查找同业务域相邻实现。
- 新增基础 CRUD 或模块骨架优先参考 `erp-generator`。
- 新增入参用 DTO，出参用 VO，持久化对象用 Entity。
- 系统间传递用 VO，跨服务调用优先复用或扩展 `erp-rpc-*`。
- 不绕过 `ApiResult` 和 `GlobalExceptionHandler`。
- 不把业务域私有逻辑放入 `erp-common`。
- 不把第三方平台 SDK 逻辑散落在业务服务中。
- 不硬编码环境地址、密钥、token、密码或外部凭证。
- 不随意使用 Mapper XML `${...}` 拼接未经控制的输入。
