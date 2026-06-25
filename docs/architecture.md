# 服务架构说明

## 架构概览

本项目是 Java 8 + Maven 多模块后端系统，采用 Spring Boot 和 Spring Cloud。`erp-gateway` 是统一入口，业务服务按领域拆分到 `erp-server-*`，模型、RPC 契约、公共能力和第三方 SDK 分别拆分到独立模块。

证据来源：

- 根 `pom.xml`
- `erp-gateway/pom.xml`
- `erp-gateway/src/main/java/com/cloud/erp/gateway`
- `erp-common/erp-common-core/src/main/java/com/common/core`
- `erp-common/erp-common-business/src/main/java/com/common/business`
- `erp-model/erp-model-sys/src/main/java/com/erp/model/sys`
- `erp-rpc/erp-rpc-sys/src/main/java/com/erp/rpc/sys/feign`
- `erp-server/erp-server-sys/src/main/java/com/erp/server/sys`
- `erp-server/erp-server-sys/src/main/resources`

## 统一入口 Gateway

`erp-gateway` 是统一入口，主要机制包括：

- 鉴权过滤：`AuthGatewayFilter`。
- 签名校验：`SignatureVerificationFilter`。
- SQL 注入过滤：`SqlInjectionFilter`。
- XSS 过滤：`XssInjectionFilter`。
- 动态路由：`DynamicRouteService`、`RouteDataSchedule`。
- 请求上下文：`GatewayContext`、`GatewayRequestContextFilter`、`GatewayResponseContextFilter`。
- 网关异常处理：`GatewayExceptionHandler`。
- Redis 支持：`ReactiveRedisConfig`。

网关不应承载业务域核心逻辑。业务规则应落在对应 `erp-server-*`。

## 典型 API 数据流

```text
Client
  -> erp-gateway
  -> erp-server-* controller/api
  -> service
  -> service/impl
  -> mapper
  -> resources/mapper/*.xml 或 MyBatis-Plus wrapper
  -> PostgreSQL/pgsql
```

约束：

- Controller 负责请求绑定、校验入口、响应包装。
- Service 负责业务编排和事务边界。
- Mapper 负责数据库访问。
- 复杂 SQL 放 Mapper XML。

## 跨服务调用流

```text
erp-server-A
  -> erp-rpc-B Feign interface
  -> erp-server-B controller/feign
  -> erp-server-B service
```

规则：

- 跨服务调用优先使用 `erp-rpc-*`。
- 新增 Feign 契约放到被调用业务域对应的 `erp-rpc-*`。
- Feign 异常处理复用 `FeignErrorDecoder` 和 `FeignServiceException`。
- 不绕过现有 RPC 模块直接散落 HTTP 调用。

## 模块边界

| 模块 | 边界 |
| --- | --- |
| `erp-gateway` | 网关、过滤、路由、上下文、网关异常。 |
| `erp-common-core` | 基础响应、基础异常、工具、校验、核心注解。 |
| `erp-common-business` | 全局异常、Feign、Redis、权限、幂等、数据权限、公共业务配置。 |
| `erp-common-message` | 消息公共能力。 |
| `erp-model-*` | Entity、DTO、VO、枚举、校验对象。 |
| `erp-rpc-*` | Feign/RPC 接口和跨服务契约对象。 |
| `erp-server-*` | 业务服务实现。 |
| `erp-sdk-*` | 第三方平台 SDK 和外部系统适配。 |
| `erp-generator` | 初始化代码模板。 |
| `erp-chrome` | 插件服务。 |

## 模型与对象流

用户确认语义：

- `Entity`：数据层对象，对应数据库表和 MyBatis-Plus 映射。
- `DTO`：入参对象。
- `VO`：出参对象。
- 系统间传递使用 `VO`。

兼容规则：

- 历史代码中存在 DTO 出参，不强制在旧接口中重命名。
- 新增接口必须按上述语义命名。

## 数据访问机制

已确认模式：

- Mapper 接口继承 MyBatis-Plus `BaseMapper<Entity>`。
- Service 接口继承 `SuperService<Entity>`。
- ServiceImpl 继承 `SuperServiceImpl<Mapper, Entity>`。
- Mapper XML 放在 `src/main/resources/mapper`。
- 简单查询使用 MyBatis-Plus lambda query 或 wrapper。
- 复杂分页、动态条件和权限 SQL 使用 XML。

注意：

- Mapper XML 中的 `${...}` 只能沿用已有受控查询模式，不应拼接未经校验的用户输入。
- 新增 Mapper 方法时同步维护 Mapper XML、DTO/VO 和 Service 调用点。

## 配置与环境

服务模块常见配置：

- `bootstrap.yml`：服务名、profile、端口、日志、Seata 基础配置。
- `bootstrap-dev.yml`、`bootstrap-test.yml`、`bootstrap-uat.yml`、`bootstrap-prod.yml`：环境配置。
- Nacos discovery/config：服务注册和配置中心。
- `logback-spring.xml`：日志配置。

约束：

- 不在业务代码硬编码环境地址或密钥。
- 新增配置项优先放到对应环境配置或 Nacos 共享配置中。

## 公共机制

### 全局异常

`GlobalExceptionHandler` 覆盖：

- `ServiceException`
- `FeignServiceException`
- 参数校验异常
- 缺少参数/请求头异常
- 方法不支持异常
- 数据库唯一键/字段长度异常
- 上传大小异常
- 运行时异常和未知异常

新增业务异常应进入既有异常处理体系。

### 权限、数据权限、幂等

可复用注解包括：

- `@RequestPermissions`
- `@DataPermission`
- `@MenuCode`
- `@Idempotent`
- `@DataIdempotent`
- `@DistributeLocker`

新增接口涉及权限、数据范围、重复提交或分布式锁时，应先查找同业务域既有用法。

### Redis、RocketMQ、XXL-JOB、Seata

- Redis/Redisson：缓存、分布式锁、网关 Redis 支持。
- RocketMQ：消息消费和同步任务，典型目录如 `rocketmq/consumer`。
- XXL-JOB：定时任务，典型目录如 `schedule` 和相关配置类。
- Seata：分布式事务，配置在 `bootstrap.yml` 中。

具体 topic/tag、job handler、事务边界命名规则仍待确认。

## 新增业务域检查清单

新增业务域或大功能时，应同时检查：

- 是否需要新增或复用 `erp-model-*`。
- 是否需要新增或复用 `erp-rpc-*`。
- 是否需要新增或复用 `erp-server-*`。
- 是否需要新增或复用 `erp-sdk-*`。
- 是否可以通过 `erp-generator` 生成初始化骨架。
- 是否需要 Gateway 路由、Nacos 配置、Seata 事务组、RocketMQ topic、XXL-JOB handler。

## 待确认

- 各业务域数据库 schema 和数据源映射。
- RocketMQ topic/tag 统一命名。
- XXL-JOB handler 统一命名。
- 生产部署拓扑。
- CI/CD 和发布流程。
