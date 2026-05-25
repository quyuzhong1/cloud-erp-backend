# 前端组件体系说明

## 当前结论

当前仓库未确认前端应用或前端组件体系。初始化分析未发现：

- `package.json`
- 前端包管理锁文件
- Vite、Webpack、Next、Vue、React 等构建配置
- `src/components` 或类似前端组件目录

证据来源：

- 仓库根目录文件列表。
- 未发现 `package.json`。
- `erp-chrome` 的 `pom.xml` 和源码显示它是 Spring Boot 服务。

## 不适用项

以下内容在当前仓库中不适用，不能编造：

- 前端框架。
- UI 组件库。
- 路由方案。
- 状态管理。
- 表单方案。
- 数据请求封装。
- 主题或样式方案。
- 组件命名、props 设计、复用方式。

## 后端可复用“组件”

虽然没有前端组件，本项目存在后端复用模块：

| 类型 | 模块 | 用途 |
| --- | --- | --- |
| 基础组件 | `erp-common-core` | 响应、异常、工具、校验、核心注解 |
| 业务公共组件 | `erp-common-business` | 全局异常、Feign、Redis、权限、幂等、数据权限 |
| 消息组件 | `erp-common-message` | 消息公共能力 |
| 模型组件 | `erp-model-*` | Entity、DTO、VO、枚举 |
| RPC 组件 | `erp-rpc-*` | Feign/RPC 契约 |
| SDK 组件 | `erp-sdk-*` | 第三方平台适配 |
| 生成模板 | `erp-generator` | 初始化代码模板 |

## Cursor 规则

- 修改前端文件前，必须先确认实际前端工程位置和技术栈。
- 不要为当前仓库假设 React、Vue、Vite、Webpack、Element、Ant Design、Pinia、Redux 等技术。
- 如果未来引入前端工程，应重新补充前端组件分层、样式、请求、路由、状态管理和测试规则。

## 建议规范

未来若新增前端工程：

- 使用单独前端根目录，避免混入后端模块。
- 先明确包管理器和构建工具。
- API 类型命名应与后端 `DTO` 入参、`VO` 出参语义对齐。
- Cursor 前端规则应使用精确 globs，仅作用于前端目录。
