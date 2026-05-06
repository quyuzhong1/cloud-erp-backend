---
name: pgsql
description: 数据库 DDL 与基础 DML 生成规范，包含表结构、通用审计字段、模块化业务字段、枚举自动化注释以及标准增删改规则。
---

# 数据库底层与数据操作规范 (DDL & DML Standard)
在进行数据库表结构设计或执行 DDL/DML 变更时，必须严格遵守以下规范。这些规范直接影响到后端代码生成、枚举自动化、前端翻译以及系统数据的审计安全性。

# Objective
1. **DDL**: 生成高标准语句，杜绝 Java 层 NPE，保证计算精度，支持业务模块按需注入与枚举自动格式化。
2. **DML**: 确保任何数据变更严格遵守乐观锁更新、逻辑删除机制及固定的测试审计人员信息。

# Workflow
**如果是 DDL 建表需求：**
1. **解析输入**：阅读文本描述或图片，提取表名与业务概念。
2. **基础注入**：强制优先注入「必选基础字段」。
3. **模块化嗅探与注入**：分析需求中是否包含特定业务场景。如果包含，必须将「模块化业务字段库」中的对应代码块完整注入。
4. **自定义字段解析**：处理其余业务字段，严格应用「字段约束与默认值强制映射表」，优先使用需求指定的默认值。
5. **🚨 附件与URL处理红线**：**若需求涉及图片、附件、文件上传或URL存储，严禁设计新表或在业务表中增加对应字段**。系统已存在公共附件关联表，只需确保业务表拥有 `id` 即可。
6. **注释格式化**：为 **每一个** 字段（包括基础字段）生成 `COMMENT`。若含枚举，按「枚举注释规范」处理。
7. **输出校验**：检查是否所有字段都有 `NOT NULL` 和 `COMMENT`。
8. **最终输出**：仅输出纯 SQL 代码块。

**如果是 DML 数据操作需求：**
1. 判断操作类型（INSERT / UPDATE / DELETE）。
2. 严格套用「标准 DML 操作规范」强制覆盖审计与控制字段。
3. 仅输出纯 SQL 代码块。

# Core Rules (Strict Constraints)

## 1. 字段约束与默认值强制映射表 (最高优先级)
* **非空原则**：所有字段必须有 `NOT NULL` 约束。
* **主键**：`"id" VARCHAR(19) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY`
* **字符串 (VARCHAR/TEXT/BPCHAR)**：必须追加 `DEFAULT '' :: CHARACTER VARYING` (或 `:: BPCHAR`)。
* **常规数字 (INT2/INT4/INT8)**：必须追加 `DEFAULT 0`。
* **金额/高精度数值**：强制使用 `DECIMAL(18, 4)`，并追加 `DEFAULT 0.0`。
* **日期时间 (TIMESTAMP)**：必须追加 `DEFAULT CURRENT_TIMESTAMP`。
* **布尔值 (BOOL)**：必须追加 `DEFAULT FALSE`。

## 2. 必选基础字段 (任何表必须放在最前)
"id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL PRIMARY KEY,
"create_user_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
"create_user_name" VARCHAR ( 50 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
"create_time" TIMESTAMP ( 6 ) NOT NULL DEFAULT CURRENT_TIMESTAMP,
"update_user_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
"update_user_name" VARCHAR ( 50 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
"update_time" TIMESTAMP ( 6 ) NOT NULL DEFAULT CURRENT_TIMESTAMP,
"version" INT4 NOT NULL DEFAULT 0,
"is_deleted" BOOL NOT NULL DEFAULT FALSE,
"remark" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

## 3. 模块化业务字段库 (按需触发注入)
**触发条件**：当需求明确或隐式包含以下业务维度时，原封不动地注入对应代码块，并为其生成对应的 COMMENT 注释。

* **【涉及单据编号】**
    "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,

* **【涉及启用禁用】**
    "disabled" bool NOT NULL DEFAULT false,

* **【涉及作废】**
    "invalid_status" BOOL NOT NULL DEFAULT FALSE,
    "invalid_remark" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及流程/审批】**
    "approve_status" VARCHAR ( 30 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'waitSubmit' :: CHARACTER VARYING,
    "approve_user_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: BPCHAR,
    "approve_user_name" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "approve_time" TIMESTAMP ( 0 ) NOT NULL DEFAULT CURRENT_TIMESTAMP,

* **【涉及来源溯源】**
    "source_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "source_code" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "source_type" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及实体仓库】**
    "warehouse_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "warehouse_name" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及SKU/商品】**
    "sku_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "sku_no" VARCHAR ( 64 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及币种与多币种】**
    "currency" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING,
    "currency_symbol" varchar(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '￥'::CHARACTER VARYING,

* **【涉及列表排序】**
    "sort" int2 NOT NULL DEFAULT 0,
    
* **【作为明细表】**
    "main_id" varchar(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING,
        
* **【作为业务关联】**
    "business_id" varchar(19) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING,
    "business_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING,
    "business_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING,

* **【涉及店铺】**
    "shop_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "shop_name" VARCHAR ( 64 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及平台】**
    "platform_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "platform_name" VARCHAR ( 64 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及销售平台】**
    "sales_platform" VARCHAR ( 100 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及客户】**
    "customer_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "customer_name" VARCHAR ( 64 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,

* **【涉及虚拟仓库】**
    "virtual_warehouse_id" VARCHAR ( 19 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    "virtual_warehouse_name" VARCHAR ( 255 ) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '' :: CHARACTER VARYING,
    
* **【涉及数量】**
    "{业务}_qty" int4 NOT NULL DEFAULT 0,

## 4. SQL 格式规范
* 必须指定所有者：`ALTER TABLE "public"."table_name" OWNER TO "postgres";`。
* **强制全字段注释**：表名及所有列名必须有 `COMMENT ON`。

## 5. 枚举与下拉项注释规范 (特殊约束)
如果需求中的字段涉及下拉项、状态选项、分类等枚举值，其字段的 COMMENT 注释必须严格按照以下键值对格式输出，**严禁使用其他格式**：
* **格式规范**：`字段解释: 枚举code1=枚举name1, 枚举code2=枚举name2`
* **命名规范**：除非用户指定，否则枚举 `code` **必须采用驼峰命名 (camelCase)**。
* **示例**：`COMMENT ON COLUMN "public"."qc"."type" IS '配置类型: productEntity=产品实物, packageAccessories=包装配件';`


## 6. 标准 DML 操作规范 (审计与并发控制)
当生成业务数据的 `INSERT`、`UPDATE` 或 `DELETE` 语句时，必须强制拼接以下底层字段赋值，不可遗漏：
* **插入 (INSERT)**:
  * **主键 ID (`id`)**: 
    1. **调用脚本**: 必须通过执行 `skills\scripts\snow_id_gen.py` 获取初始雪花算法 ID。
    2. **连续自增**: 若涉及多条数据插入且未指定 ID，则后续条目的 `id` 在第一条生成的 ID 基础上依次递增（ID_n = ID_start + n - 1）。
    3. **用户指定优先**: 若用户已明确指定 `id` 值，则不再自动生成，直接使用指定值。
  * `create_user_id` / `update_user_id`: `'1838067106149261313'`
  * `create_user_name` / `update_user_name`: `'陈锦辉'`
  * `create_time` / `update_time`: `now()`
* **更新 (UPDATE)**:
  * `update_time`: `now()`
  * `version`: `version + 1` (严格执行乐观锁累加)
* **删除 (DELETE - 强制逻辑删除)**:
  * 禁止使用物理 `DELETE` 命令，强制转为 `UPDATE`。
  * `is_deleted`: `true`
  * `update_time`: `now()`
  * `version`: `version + 1`

# Output Format
严格以 Markdown 的 SQL 代码块形式输出结果。不输出任何寒暄、解释或代码外的文本。
