# ERP 财务微服务 (FMS - Financial Management System)

## 模块说明

本模块是 ERP 系统的财务管理微服务，参考 WMS 模块结构创建。

## 创建的模块

### 1. erp-model-fms
- **路径**: `erp-model/erp-model-fms`
- **说明**: 财务系统的数据模型层
- **包结构**:
  - `dto`: 数据传输对象
  - `entity`: 实体类
  - `enums`: 枚举类
  - `vo`: 视图对象

### 2. erp-rpc-fms
- **路径**: `erp-rpc/erp-rpc-fms`
- **说明**: 财务系统的 RPC 接口层
- **包结构**:
  - `feign`: Feign 客户端接口

### 3. erp-server-fms
- **路径**: `erp-server/erp-server-fms`
- **说明**: 财务系统的服务实现层
- **包结构**:
  - `config`: 配置类（已包含 MybatisPlusConfig、XxlJobConfig）
  - `controller`: 控制器层
  - `service`: 服务层
  - `service/impl`: 服务实现层
  - `mapper`: 数据访问层
  - `convert`: 对象转换类
  - `handler`: 处理器
  - `query`: 查询对象

## 配置文件

### 服务配置
- **端口**: 9100
- **服务名**: erp-fms
- **数据库配置**: db-fms.yml (需要在 Nacos 中配置)

### 环境配置
已创建以下环境配置文件：
- `bootstrap.yml`: 基础配置
- `bootstrap-dev.yml`: 开发环境
- `bootstrap-test.yml`: 测试环境
- `bootstrap-uat.yml`: UAT 环境
- `bootstrap-prod.yml`: 生产环境

### Seata 事务组
- **事务组名**: fms_seata_tx_group

## 主要依赖

- Spring Boot
- MyBatis Plus
- PostgreSQL
- MySQL
- Seata (分布式事务)
- XXL-Job (定时任务)
- OpenFeign (远程调用)
- Nacos (服务注册与配置中心)

## 启动类

`com.erp.server.fms.ErpServerFmsApplication`

## 开发说明

1. 在 Nacos 配置中心添加 `db-fms.yml` 数据库配置文件
2. 根据业务需求在 `erp-model-fms` 中添加实体类和 DTO
3. 在 `erp-rpc-fms` 中定义对外提供的 Feign 接口
4. 在 `erp-server-fms` 中实现具体的业务逻辑

## 注意事项

1. 所有环境的配置文件已创建，但需要在 Nacos 中添加对应的数据库配置
2. 日志文件将保存在 `/logs/erp-fms` 目录下
3. 开发环境下 XXL-Job 不会被注入，其他环境需要配置相关参数

## 已完成
- [x] 创建 erp-model-fms 模块
- [x] 创建 erp-rpc-fms 模块
- [x] 创建 erp-server-fms 模块
- [x] 更新所有父 pom.xml 文件
- [x] 创建基础配置类
- [x] 创建所有环境配置文件
- [x] 创建日志配置

## 待完成
- [ ] 在 Nacos 中配置 db-fms.yml
- [ ] 根据业务需求创建数据库表
- [ ] 开发具体的业务功能

