---
name: query_gen
description: 根据提供的 **DDL 语句** 或 **Java 实体类**，全自动推断并生成 PostgreSQL 环境下 `cfg_query_condition` 和 `cfg_query_option` 表的 `INSERT` 脚本。
---

# 高级查询配置 SQL 自动生成
## 1. 技能目标 (Objective)
根据用户提供的 **DDL 语句**、**DML 语句**、**sql 语句**、**Java 实体类** 或 **指定字段列表**，全自动推断并生成 PostgreSQL 环境下 `cfg_query_condition` 表的 `INSERT` 脚本。

### 1.1 前置条件 (Preconditions)
* **按需生成**：若用户显式指定了需要配置的字段，则**仅**为这些字段生成配置。
* **尊重顺序**：必须严格遵循用户提供的字段顺序来分配 `index`（从 0 开始递增）。
* **自动补全**：若用户未指定字段，则扫描全表/全实体类生成。

---

## 2. 自动化命名规范 (Naming Convention)

### 2.1 code 自动生成
* **格式**：`{module}:{tableName}:paging`。
* **逻辑**：
    * `module`：取自主表所属的服务名（如 `wms`, `tms`, `oms` 等）。
    * `tableName`：将数据库表名转换为**小驼峰**格式。
    * *示例*：`wms` 服务下的 `sample_borrow_info` 表 $\rightarrow$ `wms:sampleBorrowInfo:paging`。

### 2.2 SQL 表别名自动生成
* **逻辑**：使用“表名单词首字母组合”。
* **规则**：
    * `qc_standard` $\rightarrow$ `qs`。
    * `qc_standard_detail` $\rightarrow$ `qsd`。
    * 在 `cfg_query_condition.value` 字段中统一使用 `{alias}.{column}` 格式。

---

## 3. ID 生成逻辑 (Identity Strategy)
* **优先规则**：若用户显式给定起始 ID（如：“从 ID 200.. 开始”），则直接以此起始值手动递增。
* **兜底规则 (执行脚本)**：若用户**未给定** ID，AI **必须**执行项目路径下的脚本：`skills\scripts\snow_id_gen.py` 以获取当前毫秒级起始 ID，严禁凭空模拟。
* **异常处理**：若脚本执行失败或未找到该脚本，**必须立即向用户报告错误原因**（如 Python 环境缺失、路径无效等），并请用户手动指定起始 ID。
* **计算原理**: 基于 `our_epoch = 1288834974657` 和 `shard_id = 5` 的 Snowflake 算法。



---

## 4. 字段解析与配置规则 (Field Logic)

### 4.1 data_type 与 controls 映射
* **布尔 (boolean)** $\rightarrow$ `controls: select`, `data_type: boolean`
* **字符 (string)** $\rightarrow$ 若字段名称包含 `id`, `type`, `status` 则设置为 `controls: select`，否则设置为 `controls: input`；`data_type: string`
* **数值 (number)** $\rightarrow$ `controls: input`, `data_type: number`
* **日期 (date)** $\rightarrow$ `controls: date`, `data_type: date`, `date_type: date`

### 4.2 动态检索逻辑 (Dynamic Lookup)
**仅当 `controls: select` 时**，必须实时检索 `skills/data/query_option_lite.md` Markdown 表格，根据字段含义动态获取 `query_option_id`。

| 业务场景 / 字段特征 | 匹配逻辑 | 优先级 ID |
| :--- | :--- | :--- |
| **审核状态** | 字段为 `approve_status` | `1742864822164201473` |
| **人员/用户相关** | 包含 `person`, `user`, `create_user`, `_by` 等 | `1742885076630179841` |
| **其他业务字典** | 根据注释中的 `type=` 或 `key=` 匹配 `url` 参数，或模糊匹配 `name` | 从 `skills/data/query_option_lite.md` 检索 |
| **显式指定** | 若注释中已存在 `query_option_id = xxx` | 直接填充该值 |

**🚨 核心映射准则**：若匹配到 `query_option_id`，`value` 必须优先映射表中的 `xx_id` 字段（而非 `xx_name`），以保证查询精确性。

**🚨 强制注释**：若匹配并使用了 `query_option_id`，生成的 `INSERT` 脚本上方必须添加一行注释以描述该 option，格式：`-- option: {id}, {api_url}, {api_name}`。

### 4.3 静态字典与特殊字段逻辑
* **跳过检索逻辑 (Skip Dynamic Lookup)**：以下场景**不执行** `skills/data/query_option_lite.md` 检索，直接填充 `option_list`：
    * **布尔类型 (boolean)** 且注释包含“是否” $\rightarrow$ `[{"label":"是","value":true},{"label":"否","value":false}]`。
    * **特定字段名 `disabled`** $\rightarrow$ `[{"label":"启用","value":true},{"label":"停用","value":false}]`。
    * **特定字段名 `invalid_status`** $\rightarrow$ `[{"label":"已作废","value":true},{"label":"未作废","value":false}]`。
* **静态转换**：注释含 `key=value` (如 `auto=自动`) 且不包含 `XXXEnum` 时 $\rightarrow$ 转换为 JSON 填充 `option_list`。
* **枚举类处理 (自动生成 Option)**：若 `query_option_id` 未能查询出来并且字段注释包含 `XXXEnum`，则生成 `cfg_query_option` 插入脚本示例：
    ```sql
    INSERT INTO "public"."cfg_query_option" ("id", "create_user_id", "create_user_name", "create_time", "update_user_id", "update_user_name", "update_time", "version", "is_deleted", "api_name", "api_url", "request_method", "param", "select_label", "select_value", "select_disabled", "search_key_field", "props", "api_type") VALUES ('{id}',  '1838067106149261313', '陈锦辉', now(), '1838067106149261313', '陈锦辉', now(), 0, 'f', '退货来源', '/{module}/common/enumDropDown?type={XXX}', 'get', '{}', 'value', 'code', 'disabled', '', '{}', '');
    ```
* **冲突处理**：同时存在 `key=value` 和 `枚举：XxxEnum` 时，**枚举逻辑优先**，`option_list` 填充空占位 `[{"label":"","value":""}]`。

### 4.4 必带默认页面配置 (tab & export)
每个页面必须默认生成以下两行特殊配置：
* **页签配置 (tab)**:
    - `value`: `'tab'`
    - `label`: `'tab列表'`
    - `controls`: `'input'`
    - `data_type`: `'string'`
    - `index`: `1000`
    - `is_extend`: `true`
    - `display_type`: `'tab'`
* **导出配置 (export)**:
    - `value`: `{alias}.id` (如 `qs.id`)
    - `label`: `'导出ids'`
    - `controls`: `'input'`
    - `data_type`: `'string'`
    - `index`: `2000`
    - `is_extend`: `false`
    - `display_type`: `'export'`

---

## 5. 默认审计字段值
* `create_user_id` / `update_user_id`: `'1838067106149261313'`。
* `create_user_name` / `update_user_name`: `'陈锦辉'`。
* `create_time` / `update_time`: `now()`。
* `group_name`: `'default'`。
* `is_extend`: `false`。
* `props`: `{}`。
* `display_type`: `'query'`。
* `option_list`: `'[{"label":"","value":""}]'`。

---

## 6. 规定 SQL 模板 (Strict Template)
**必须严格按照以下模板和字段顺序生成：**

```sql
-- option: {id}, {api_url}, {api_name}
INSERT INTO "public"."cfg_query_condition" ("id", "create_user_id", "create_user_name", "create_time", "update_user_id", "update_user_name", "update_time", "version", "is_deleted", "code", "value", "label", "query_option_id", "controls", "data_type", "date_type", "props", "index", "is_extend", "group_name", "display_type", "option_list") VALUES ('{id}', '1838067106149261313', '陈锦辉', now(), '1838067106149261313', '陈锦辉', now(), 0, 'f', '{code}', '{value}', '{label}', '{query_option_id}', '{controls}', '{data_type}', 'date', '{}', {index}, '{is_extend}', 'default', '{display_type}', '{option_list}');
```

---

## 7. skills/data/query_option_lite.md 自动化维护
AI 必须主动维护并增量更新 `skills/data/query_option_lite.md` Markdown 表格引用库。

### 7.1 提取逻辑
当监听到 `INSERT INTO "public"."cfg_query_option"` 时，提取：
* **id**: SQL `id` 值。
* **name**: SQL `api_name` 值。
* **url**: SQL `api_url` 值。
* **module**: 提取 `api_url` 首个路径段。

### 7.2 查重逻辑
检查 `skills/data/query_option_lite.md` 是否已存在相同 `id` 或 **标准化 `url`**（去除首斜杠）。
* **若存在**：不处理。
* **若不存在**：按现有 Markdown 表格列顺序追加一行：`| id | name | module | \`url\` |`。