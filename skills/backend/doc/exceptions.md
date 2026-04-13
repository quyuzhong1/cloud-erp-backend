---
name: exceptions
description: 全局异常处理与国际化规范，包含业务域前缀分区、错误码区间分配、I18n 自动化映射及 Service 层断言准则。
---

# 全局异常处理与国际化规范 (Exception & I18n Standard)

## 1. 核心理念
* **零硬编码**: 严禁在业务逻辑中直接写错误字符串，必须通过 ApiError 注册并且生成国际化 `messages_*.properties`。
* **Key 映射原则**: messages_*.properties 中的 Key 必须与 ApiError 的枚举变量名严格一致。
* **占位符一致性**: 统一使用 Java 标准占位符 {0}, {1}...，严禁使用 %s 或 {}。

## 2. 维护与变更规范 (🚨 核心红线)
* **严禁修改 (Stability)**: 🚨 **不允许修改或调整原来的错误码**。为了保证历史数据和日志的可追溯性，禁止更改已存在枚举项。
* **优先复用 (Search First)**: 在设计新错误码前，必须全量检索 `ApiError.java`。若已存在语义相同或可通用的错误码，**必须直接使用**，严禁重复生成含义相同的错误码。
* **就近原则 (Locality)**: 写入新的错误码时，位置必须是在 **相关的错误码下面增加**。严禁随意插在文件末尾或无关业务块中，确保逻辑聚拢。
* **ID 唯一性**: 新增错误码前必须检查 ApiError.java，严禁 ID 重复。
* **去重防线 (Duplication Check)**: 生成数据到 `ApiError` 或国际化文件 `messages_*.properties` 时，**必须强制检查是否存在重复的枚举名或 Key**。如果发现重复，**严禁自动覆盖或盲目跳过，必须立即暂停操作并明确提示用户，由用户决定下一步动作**，确保绝对不重复生成数据。

## 3. 错误码区间与业务域划分
错误码按业务模块进行物理隔离，新增枚举时必须遵守对应的前缀与区间。

### 3.1 系统与基础区 (0 - 1999)
* **HTTP_ (000-599)**: 标准 HTTP 状态码及网关异常。
* **WARNING_ (600-699)**: 业务警告信息（需前端弹窗确认）。
* **QUERY_ (700-799)**: 高级查询引擎异常。
* **COMMON_ (1000-1999)**: 通用校验、参数缺失、配置缺失等跨模块错误。

### 3.2 业务逻辑区 (2000 - 99999)
* **包含前缀**: AUTH_, EMAIL_, FILE_, BILL_, DMP_, WF_, PROJECT_, PRODUCT_, BOM_, MOULD_, BI_, MAPPING_, SHOP_, SUPPLIER_, SALES_DEMAND_, PO_, PURCHASE_PRICE_, SO_, WH_, FIRST_MILE_, SAMPLE_, VM_, CUSTOMER_, LOGISTICS_, FIN_, TRIAL_CALC_ 等。

## 4. 命名与占位符规范
* **枚举命名**: 采用 DOMAIN_ACTION_RESULT 格式。
* **命名规范**: 除非需求指定，否则枚举变量关联的 code 字符串必须采用**驼峰命名 (camelCase)**。
* **占位符顺序**: properties 中的占位符顺序必须与参数传入顺序严格一致。

## 5. 开发实现准则
* **异常抛出**: 业务逻辑仅允许抛出 ServiceException，推荐使用 BizAssert 工具类。
* **参数传递**: 抛出时必须传入关键业务主键（如 skuCode）以便 I18n 填充。
* **Feign 透传**: 微服务间调用必须透传原始错误码。

