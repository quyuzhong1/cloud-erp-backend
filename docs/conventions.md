# 项目规范

## 证据来源

- 根 `pom.xml` 和各模块 `pom.xml`
- `erp-common/erp-common-core/src/main/java/com/common/core/controller/BaseController.java`
- `erp-common/erp-common-business/src/main/java/com/common/business/config/GlobalExceptionHandler.java`
- `erp-common/erp-common-business/src/main/java/com/common/business/annotation`
- `erp-server/erp-server-sys/src/main/java/com/erp/server/sys`
- `erp-model/erp-model-sys/src/main/java/com/erp/model/sys`
- `erp-rpc/erp-rpc-sys/src/main/java/com/erp/rpc/sys/feign`
- `erp-server/erp-server-sys/src/main/resources/mapper`

## 命名与结构

### 已确认

- Maven 模块使用 `erp-*` 命名。
- 业务域在 `erp-model`、`erp-rpc`、`erp-server` 中保持对应拆分。
- Java 类使用 PascalCase。
- Controller 以 `Controller` 结尾。
- Service 接口以 `Service` 结尾。
- Service 实现以 `ServiceImpl` 结尾。
- Mapper 以 `Mapper` 结尾。
- Mapper XML 与 Mapper 接口同名，放在 `src/main/resources/mapper`。

## 模型规范

### 用户确认

- `Entity` 表示数据层。
- `DTO` 表示入参。
- `VO` 表示出参。
- 系统间传递使用 `VO`。

### 代码确认

- `erp-model-*` 按业务域放置模型。
- 常见目录包括 `entity`、`dto`、`vo`、`enums`、`validator`。
- Entity 使用 MyBatis-Plus 注解，如 `@TableName`、`@TableField`。
- 模型类常用 Lombok，如 `@Data`、`@Getter`、`@Setter`、`@NoArgsConstructor`。

### 兼容要求

- 历史代码中存在 DTO 出参。修改旧接口时保持兼容，不为命名规范强制破坏接口。
- 新增代码按 `DTO=入参`、`VO=出参/系统间传递` 执行。

## API 规范

- 面向 Web/API 的接口优先放在 `controller/api`。
- 服务间调用入口放在 `controller/feign`。
- 特定客户端入口按现有目录放在 `controller/app` 或 `controller/pda`。
- Controller 返回 `ApiResult`。
- Controller 优先继承 `BaseController` 并使用 `success()`、`failure()`。
- 参数绑定使用 `@RequestBody`、`@RequestParam` 等 Spring MVC 注解。
- 参数校验使用 `@Validated` 或 Bean Validation。
- 操作日志按既有方式使用 `@LogSystemModule`、`@LogAction`。

## Service 与 Mapper 规范

- Service 接口按现有模式继承 `SuperService<Entity>`。
- ServiceImpl 按现有模式继承 `SuperServiceImpl<Mapper, Entity>`。
- Mapper 接口按现有模式继承 `BaseMapper<Entity>`。
- 简单 CRUD 和简单查询优先使用 MyBatis-Plus。
- 复杂查询、分页、动态 SQL 放在 Mapper XML。
- 新增 Mapper 方法时同步维护 Mapper XML 和调用方。
- Mapper XML 中谨慎使用 `${...}`，不得拼接未经控制的用户输入。

## 错误处理

- 本地业务异常使用 `ServiceException`。
- Feign 调用异常使用 `FeignServiceException`。
- 统一由 `GlobalExceptionHandler` 转换为 `ApiResult`。
- 不新增独立响应格式或异常处理体系。
- Controller 不堆叠复杂 try/catch，业务失败交给服务层和全局异常处理。

## 日志、权限、幂等

优先复用现有公共机制：

- 日志：`@LogSystemModule`、`@LogAction`。
- 权限：`@RequestPermissions`、`@MenuCode`。
- 数据权限：`@DataPermission`。
- 幂等：`@Idempotent`、`@DataIdempotent`。
- 分布式锁：`@DistributeLocker`。

新增接口涉及这些能力时，先查找同业务域现有用法。

## Feign 与外部调用

- 跨服务调用优先复用或扩展 `erp-rpc-*`。
- Feign 配置优先复用 `FeignInterceptor`、`FeignErrorDecoder`、`FeignTimeoutConfig`。
- 第三方平台能力优先放到 `erp-sdk-*`。
- 不把第三方签名、重试、请求封装散落在业务服务。

## 配置规范

- 服务基础配置使用 `bootstrap.yml`。
- 环境配置使用 `bootstrap-dev.yml`、`bootstrap-test.yml`、`bootstrap-uat.yml`、`bootstrap-prod.yml`。
- Nacos discovery/config 按现有模块配置。
- 日志配置使用 `logback-spring.xml`。
- 不在业务代码硬编码环境地址、账号、密码、token、密钥或外部凭证。

## 测试规范

- Java 测试放在对应模块 `src/test/java`。
- 测试类常见命名为 `*Test` 或 `*Tests`。
- 优先运行模块级命令：

```powershell
mvn -pl <module> -am test
```

- 如果模块配置 `maven.test.skip=true`，需要确认测试是否实际执行。
- 涉及 Nacos、Redis、PostgreSQL、RocketMQ、Seata、XXL-JOB 或第三方 SDK 的测试，应明确依赖环境。

## 待确认

- 统一格式化工具。
- 统一静态检查工具。
- 提交前强制命令。
- CI/CD 流程。
