# Cloud ERP Backend Agent Guide

## 项目概览

本仓库是 Cloud ERP 后端 Maven 多模块项目。项目以 `erp-gateway` 作为统一入口，按公共能力、模型、RPC 契约、业务服务、第三方 SDK 和插件服务拆分模块。

证据来源：

- 根目录 `pom.xml`
- `README.md`
- `erp-gateway/pom.xml`
- `erp-common/**/pom.xml`
- `erp-model/**/pom.xml`
- `erp-rpc/**/pom.xml`
- `erp-server/**/pom.xml`
- `erp-sdk/**/pom.xml`
- `erp-chrome/pom.xml`

当前仓库未发现 `package.json` 或前端构建配置，因此不要为本仓库编造前端框架、组件库、路由、状态管理或样式规范。

## 技术栈

代码确认与用户确认的技术栈：

- Java 8
- Maven
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Nacos
- Seata
- RocketMQ
- PostgreSQL/pgsql
- Redis/Redisson
- XXL-JOB
- Lombok
- MyBatis
- MyBatis-Plus
- FastJSON/FastJSON2
- MapStruct
- EasyExcel

待确认：

- 统一 CI 流程
- 统一 Lint/Format 工具
- 全量测试策略和提交前检查命令

## 运行与测试命令

优先使用 Maven 模块级命令做局部验证：

```powershell
mvn clean compile
mvn test
mvn -pl <module> -am compile
mvn -pl <module> -am test
```

注意：

- 部分模块配置了 `maven.test.skip=true`，测试是否执行需要按模块确认。
- 启动具体服务通常依赖 Nacos、Redis、PostgreSQL、RocketMQ、Seata、XXL-JOB 等外部环境。
- 当前未确认统一格式化或静态检查命令。

## 架构说明

主要模块职责：

| 模块 | 职责 |
| --- | --- |
| `erp-gateway` | 统一入口网关，包含鉴权、签名校验、SQL/XSS 过滤、动态路由、请求上下文和网关异常处理。 |
| `erp-common-core` | 基础响应、异常、工具、校验、核心注解。 |
| `erp-common-business` | 全局异常、Feign 配置、Redis 配置、权限/幂等/数据权限/业务注解等公共业务能力。 |
| `erp-common-message` | 消息相关公共能力。 |
| `erp-model-*` | 各业务域模型，包含 `entity`、`dto`、`vo`、`enums` 等。 |
| `erp-rpc-*` | 各业务域 Feign/RPC 契约。 |
| `erp-server-*` | 各业务域服务实现。 |
| `erp-sdk-*` | 第三方平台和外部系统 SDK。 |
| `erp-generator` | 初始化代码生成模板。 |
| `erp-chrome` | Spring Boot 插件服务，不是前端工程。 |

典型请求链路：

```text
Client -> erp-gateway -> erp-server-* Controller -> Service -> Mapper -> Database
                              |
                              +-> erp-rpc-* Feign -> another erp-server-*
                              |
                              +-> erp-sdk-* -> third-party service
```

## 后端规则

- 新增 API 遵循 `controller -> service -> service/impl -> mapper -> mapper XML`。
- Controller 优先继承 `BaseController`，统一返回 `ApiResult`。
- 业务异常使用 `ServiceException`，Feign 异常使用 `FeignServiceException`，由 `GlobalExceptionHandler` 统一处理。
- Service 接口在 CRUD 场景下延续 `SuperService<Entity>`。
- ServiceImpl 在 CRUD 场景下延续 `SuperServiceImpl<Mapper, Entity>`。
- Mapper 在 CRUD 场景下继承 MyBatis-Plus `BaseMapper<Entity>`。
- 简单查询优先使用 MyBatis-Plus wrapper；复杂分页和动态 SQL 放入 `src/main/resources/mapper/*.xml`。
- 跨服务调用优先复用或扩展 `erp-rpc-*` 的 Feign 接口。
- 第三方平台能力优先复用或扩展 `erp-sdk-*`。
- 初始化 CRUD 或模块骨架优先参考 `erp-generator` 模板。

## 模型规则

用户确认的语义：

- `Entity` 表示数据层对象。
- `DTO` 表示入参。
- `VO` 表示出参。
- 系统间传递使用 `VO`。

兼容说明：

- 历史代码中存在部分 DTO 用作出参。修改旧接口时保持兼容；新增代码按上述语义执行。

## 前端规则

当前仓库未确认前端应用。不要根据通用经验添加 React、Vue、Vite、Webpack、UI 库、状态管理或样式体系规则。若未来出现前端工程，必须先读取实际前端配置后再补规则。

## 测试规则

- Java 测试放在对应模块 `src/test/java`。
- 优先运行模块级 Maven 测试。
- 涉及 Nacos、Redis、PostgreSQL、RocketMQ、Seata、XXL-JOB 或第三方 SDK 凭证的测试，必须明确依赖环境。
- 不能运行测试时，说明原因和未验证风险。

## 开发注意事项

- 保持模块边界，不把单一业务域私有逻辑放入 `erp-common`。
- 优先复用已有 DTO、VO、Entity、Feign、Service、Mapper、工具类和公共注解。
- 不新增独立响应结构、异常体系、ORM 方式或未确认的新框架。
- 不在业务代码硬编码环境地址、密钥、token、密码或第三方凭证。
- Mapper XML 中谨慎使用 `${...}`，只沿用已有安全封装和查询辅助模式。
- 修改已有接口时保持路径、返回结构、字段语义和错误码兼容。

## 黄金实践

- 新增业务功能前，同时检查对应 `erp-model-*`、`erp-rpc-*`、`erp-server-*`、`erp-sdk-*`。
- 新增 API 时先找同业务域相邻 Controller 和 Service，保持命名、注解、响应和异常风格一致。
- 新增服务逻辑放在 Service 层，Controller 保持薄层。
- 新增跨服务调用先扩展 RPC 契约，不直接绕过现有 Feign 体系。
- 新增第三方平台能力先检查 SDK 模块，不把平台签名、重试、请求封装散落到业务服务。
