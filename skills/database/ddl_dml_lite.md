# DDL & DML 核心约束 (Lite)

## 🚨 数据库红线 (Redlines)
1. **强制非空**: 所有字段必须 `NOT NULL`。
2. **审计注入**: 任何表头部必须按序包含 10 个基础审计字段（id 至 remark）。
3. **附件禁令**: 严禁在业务表开图片/URL 字段，必须复用公共附件表。
4. **注释契约**: 全字段必须有 `COMMENT`。枚举格式必须为 `解释: code1=名1, code2=名2`。

## 1. 字段约束映射表
| 类型 | 强制后缀 / 默认值 | 示例 |
| :--- | :--- | :--- |
| **主键** | `VARCHAR(19) NOT NULL PRIMARY KEY` | `"id"` |
| **字符串** | `DEFAULT '' :: CHARACTER VARYING` | 姓名、备注 |
| **金额/精度** | `DECIMAL(18, 4) DEFAULT 0.0` | 价格、重量 |
| **日期时间** | `TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP` | 业务时间 |
| **布尔值** | `BOOL DEFAULT FALSE` | 状态开关 |

## 2. 模块化业务字段触发器
当需求涉及以下关键词时，**必须完整注入**对应代码块：
* **【单据/编号】**: `"code" varchar(50) NOT NULL DEFAULT ''`
* **【流程/审批】**: `approve_status` (默认 'waitSubmit'), `approve_user_id`, `approve_user_name`, `approve_time`
* **【仓库/实物】**: `warehouse_id`, `warehouse_name`, `sku_id`, `sku_no`
* **【来源/溯源】**: `source_id`, `source_code`, `source_type`

## 3. 标准 DML 逻辑
- **INSERT**: ID 必须调用 `antigravity-skills\automation\snow_id_gen.py` 生成。
