# 项目级黄金实践

## 新增功能

- 先定位业务域，再检查对应 `erp-model-*`、`erp-rpc-*`、`erp-server-*`、`erp-sdk-*`。
- 基础 CRUD 或模块初始化优先参考 `erp-generator` 模板。
- 不跨域随意放置代码。
- 业务实现放在对应 `erp-server-*`。
- 只有多个业务域真实复用的能力才放入 `erp-common`。

## 新增 API

- Controller 保持薄层，只做请求绑定、校验入口和响应包装。
- 入参使用 DTO。
- 出参使用 VO。
- 持久化对象使用 Entity。
- 系统间传递使用 VO。
- 返回统一使用 `ApiResult`。
- 业务失败进入既有异常体系，不创建新的响应结构。
- 需要操作日志时复用 `@LogSystemModule` 和 `@LogAction`。
- 涉及权限、数据权限、幂等或分布式锁时，优先复用现有注解。

## 新增服务逻辑

- Service 负责业务编排和事务边界。
- Controller 不直接访问 Mapper。
- 简单查询优先使用 MyBatis-Plus wrapper。
- 复杂 SQL 放 Mapper XML。
- 分布式事务需求先确认现有 Seata 配置和同域事务模式。
- 缓存和分布式锁优先复用 Redis/Redisson 公共配置。
- 异步或消息场景先查找 RocketMQ 既有 consumer/sync 用法。
- 定时任务先查找同模块 `schedule` 和 XXL-JOB 配置。

## 新增模型

- 新增字段前检查已有 DTO、VO、Entity 是否已表达相同含义。
- 修改已有字段时评估接口兼容性、跨服务影响和 Mapper XML。
- 历史 DTO 出参不强制改名；新增接口按 `DTO=入参`、`VO=出参` 执行。
- Entity 不承载接口展示专用字段，展示字段应放 VO。

## 跨服务与外部服务

- 跨服务调用优先复用 `erp-rpc-*` Feign 接口。
- 新增 Feign 接口放在对应业务域的 `erp-rpc-*`。
- Feign 异常处理复用 `FeignErrorDecoder` 和 `FeignServiceException`。
- 第三方平台逻辑优先复用或扩展 `erp-sdk-*`。
- 不把平台签名、token 刷新、重试、请求封装散落到业务服务。

## 修改已有功能

- 先阅读同目录相邻代码，保持命名、分层、响应、异常和 Mapper 风格一致。
- 保持已有 API 路径、返回结构、字段语义和错误码兼容。
- 不随意移动公共类或改变 Maven 模块依赖方向。
- 不引入未确认的新框架、新响应格式、新 ORM 方式或前端技术栈。
- 修改 Mapper XML 时同步检查 Mapper 接口、DTO/VO、Service 和调用方。

## 安全与配置

- 不硬编码环境地址、账号、密码、token、密钥或第三方凭证。
- 不在日志中输出敏感信息。
- Mapper XML 中不得使用 `${...}` 拼接未经控制的用户输入。
- 新增配置优先放入环境配置或 Nacos 配置体系。

## 测试与验证

- 优先使用模块级 Maven 编译或测试验证变更。
- 依赖外部中间件的测试，明确 Nacos、Redis、PostgreSQL、RocketMQ、Seata、XXL-JOB 等环境要求。
- 如果测试因环境或配置无法运行，应记录原因和未验证风险。

## 应避免

- 避免在 `erp-common` 中加入单一业务域私有逻辑。
- 避免绕过 `ApiResult` 和 `GlobalExceptionHandler`。
- 避免 Controller 中堆放业务逻辑。
- 避免新增重复 DTO/VO/Entity。
- 避免在业务代码硬编码环境信息或凭证。
- 避免编造当前仓库不存在的前端规范、Lint 规则或 CI 流程。
