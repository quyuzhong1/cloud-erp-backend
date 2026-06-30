---
name: fs-cfg-gen
description: Generate workflow cfg_query_option and cfg_query_option_ext from Entity Java source. Two-phase flow — preview and ask questions first, generate INSERT SQL only after user confirms. Use for third-notice, approve rules, SpEL field mapping.
---

# 飞书配置cfg_query_option 生成（Skill 专用）

## 定位

**唯一实现方式**：Cursor Agent 读本 Skill + 读 Entity 源码 + 读 `.cursor/data/query_option*.md` + 读 [`.cursor/data/project-context.md`](../../data/project-context.md)，**先预览、先提问、待用户确认后再生成 SQL**。  
**不**调用、不依赖 Java 批量生成代码。

消费方：`ThirdNoticePushRecordServiceImpl`（通知正文）、`cfg_rule_condition`（SpEL）、审批同步字段映射；明细通过 `parent_id` + FeignQuery 拉子表。

---

## 工作流（强制两阶段）

```text
阶段 A：分析 + 预览 + 提问  →  等待用户确认  →  阶段 B：生成 SQL
```

| 阶段 | 何时 | Agent 做什么 | 禁止做什么 |
|------|------|--------------|------------|
| **A. 确认前** | 用户提出生成需求后 | 读 Entity / query_option；输出字段预览表、待确认清单、**疑问项**；对不确定处**主动提问** | **禁止**输出 INSERT/UPDATE SQL；**禁止**跑 `snow_id_gen.py`；**禁止**分配雪花 id |
| **B. 确认后** | 用户明确表示「没问题 / 可以生成 SQL / 确认」 | 跑 `snow_id_gen.py`（或用户给定 START_ID）；输出 ID 分配表 + 完整 SQL + 验收 SQL | **禁止**声称已执行 SQL；生产库须用户另行授权 |

**有疑问或理解不充分时，必须先提问，不得猜测后直接写 SQL。**

典型须暂停并提问的情况：

- String 字段对应 **enum / dict / class** 多种可能（如 `orderType` 是 `OrderTypeEnum` 还是 `salesOrderType` 字典）
- `api_url` 在 `query_option.md` 中 **多条匹配** 或 **无匹配**
- 外键 `*Id` **无法唯一**解析关联 Entity / 跨模块
- 用户参数缺失或与 Entity 不一致（表名、module、bussinessKey）
- 库中 **已有配置** 与本次推断冲突，merge 策略不明
- 是否需要明细表、`parent_id`、特殊字段（如 `noticeNode`）

提问应**具体、可选项清晰**（给出 A/B 方案及推荐理由），一次收齐同类问题，避免碎片化。

用户确认方式示例：「没问题，生成 SQL」「按方案 A」「orderType 用 OrderTypeEnum」等。**未收到明确确认前，默认停留在阶段 A。**

若用户首轮即要求「直接给 SQL」，仍须先输出**精简预览 + 待确认项**；若有任何 open question，必须先问，**不得**因用户催促而跳过。

---

## 用户需提供

| 参数 | 说明 | 示例 |
|------|------|------|
| `model` | 模块，对应 `com.erp.model.{model}.entity` | `tms`、`fms` |
| `tableName` | `@TableName`，多表逗号分隔 | `logistics_bill` |
| `fieldBelongsType` | 与表一一对应 | `main` 或 `main,detailList` |
| `bussinessKey` | 流程/菜单 key | `logisticsBill` |
| `useType` | 一般 `allData` | `allData` |

可选：目标环境（dev）、起始 ID（用户指定时跳过脚本）、是否只预览不写 SQL。

---

## 项目默认配置（必读）

生成 **cfg_query_option / cfg_query_option_ext** 的 INSERT SQL 时，必须遵循 [`.cursor/data/project-context.md`](../../data/project-context.md)：

### SQL 默认审计字段

| 字段 | 默认值 |
|------|--------|
| `create_user_id` | `0` |
| `update_user_id` | `0` |
| `create_user_name` | `system` |
| `update_user_name` | `system` |
| `create_time` | `now()` |
| `update_time` | `now()` |
| `version` | `0` |
| `is_deleted` | `false` |

- 用户明确指定时以用户为准。
- **禁止**在 Java 业务代码中硬编码；**仅**用于 INSERT/UPDATE 配置脚本。

### cfg_query_option 业务列默认值（禁止 NULL）

生成 INSERT 时必须遵循 [project-context §2.1](../../data/project-context.md)，与后台 `CfgQueryOptionServiceImpl#add` 一致：

| 字段 | 无 api / 无主表关联时 | 有 api 时 |
|------|------------------------|-----------|
| `parent_id` | `''` | 明细行填主表 `id` 的 option id |
| `api_url` | `''` | 实际路径 |
| `request_method` | `get` | `get` / `postJson` 等 |
| `select_label` | `value` | 按 `query_option.md` |
| `select_value` | `code` | 按 `query_option.md` |
| `select_disabled` | `disabled` | `disabled` |
| `logic` | `==` | `==` |
| `controls` | `''` | `''` |
| `extend_type` | `''` | 仅 noticeNode 等特殊字段填枚举 code |

**禁止**在 INSERT 中对上表列写 `NULL`。无远程下拉的 `input`/`date` 字段仍须写出 `''`、`get`、`value`、`code`、`disabled`。

### 主键 ID 生成（用户未指定时）

1. 执行仓库脚本（路径见 [project-context §3](../../data/project-context.md)）：

```bash
python tools/ai/snow_id_gen.py
# Windows 若无 python 命令：py tools/ai/snow_id_gen.py
```

2. 输出值作为 **起始 ID**（记为 `START_ID`）。
3. 所有待插入行（`cfg_query_option` + `cfg_query_option_ext`）在同一批次内 **连续递增**：第 1 行 `START_ID`，第 2 行 `START_ID+1`，…（整数 +1，不用字符串拼接）。
4. 在 SQL 注释中注明：`-- START_ID from snow_id_gen.py: {START_ID}`。
5. **禁止**凭空伪造雪花 ID；脚本不存在或执行失败时，**必须**提示用户手动提供起始 ID，不得继续编造 id。
6. 用户已给出起始 ID 或要求复用库中已有 id 时，不跑脚本。

**分配顺序建议**：先按生成顺序列出全部 `cfg_query_option` id，再按 ext 引用顺序列出 `cfg_query_option_ext` id（ext 的 `cfg_query_option_id` 填对应 option 的 id）。

---

## Agent 执行步骤

### 1. 定位 Entity

- 在 `erp-model/erp-model-{model}/src/main/java/com/erp/model/{model}/entity/` 搜索 `@TableName("表名")`。
- **必须**带 `model` 限定；禁止无 module 猜表（如 `dict_basic` 冲突）。
- 记录：`classpath` = `"class " + 全限定类名`（保留 `class ` 前缀）。

### 2. 解析字段（Entity 为准）

反射/读源码，遍历字段（含父类至 `BaseEntity` 为止），应用排除与推断：

**排除**

- 审计：`createUserId`、`createUserName`、`createTime`、`updateUserId`、`updateUserName`、`updateTime`、`version`、`isDeleted`
- 明细块：不生成 `mainId`
- 冗余展示字段（同 Entity 存在配对 Id 时跳过）：
  - `*UserName` ↔ `*UserId`
  - `*DeptName` ↔ `*DeptId`
  - `*OrgName` ↔ `*OrgId`
  - `*Name` ↔ `*Id`（**例外**：`productName` 等无 Id 配对保留）

**中文名**

- 类 JavaDoc → `table_cn_name`
- 字段 JavaDoc → `condition_field_name`；无注释用 Java 属性名

**field_type / value_type / logic**

| Java 类型 | field_type | value_type | logic |
|-----------|------------|------------|-------|
| enum | `radioV2` | 枚举类 simpleName | `==` |
| Boolean/boolean | `radioV2` | `Boolean` | `==` |
| LocalDate/LocalDateTime | `date` | 类型名 | `==` |
| 数值类型 | `number` | 类型名 | `==` |
| BigDecimal | `amount` | `BigDecimal` | `==` |
| String 等 | `input` | `String` | `==` |

`is_required` 默认 `false`。`controls` 可空。

**api_url**

- 读 [`.cursor/data/query_option.md`](../../data/query_option.md) 或 [lite 版](../../data/query_option_lite.md) 按 module + 字段语义匹配。
- 审批状态常见：`/{model}/drop/down/approveStatus/list`（与 ext 独立）。

### 3. cfg_query_option_ext

| 条件 | type | class_path / data_json |
|------|------|------------------------|
| Java enum 字段 | `enum` | `class_path`=枚举 FQCN；`data_json`=`{"enumName":"ApproveStatus","sysClassify":"fms"}` |
| Boolean | `bool` | `[{"label":"是","value":"true"},{"label":"否","value":"false"}]` |
| `*UserId` / `createUserId` | `user` | `{}` |
| `*DeptId` | `dept` | `{}` |
| 外键 `*Id`（可解析关联 Entity） | `class` | `class_path`=关联 Entity FQCN；`data_json`=`{"sysClassify":"scm","select":"name","tableName":"supplier","condition":"id"}` |
| String + 存在 `getXxxEnum()` | `enum` | 同 enum 规则；**列入待确认** |

**enumName 规则（与 `EnumCacheUtils` 一致）**

- `ApproveStatusEnum` → `ApproveStatus`（去掉 `Enum` 后缀）
- **禁止**写 `ApproveStatusEnum`

### 4. 主表 → 明细顺序

1. 先输出主表 `fieldBelongsType=main` 全部字段（含 `id`）。
2. 再输出明细 `detailList`；每条明细 option 的 `parent_id` = 主表 `condition_field=id` 那条 option 的 **id**（若库中已有则查库取值）。
3. 明细不生成 `mainId`。

### 5. 查重 / merge 策略

执行 INSERT 前，Agent 应给出查询 SQL：

```sql
SELECT id, condition_field, field_type, api_url
FROM cfg_query_option
WHERE bussiness_key = '{bussinessKey}' AND use_type = '{useType}'
  AND table_name = '{tableName}';
```

- 自然键：`(bussinessKey, useType, tableName, conditionField)`
- **已存在且 `field_type`/`api_url` 非空**：不覆盖，仅提示「已有人工配置」
- **已存在但关键字段为空**：给出 `UPDATE` 补空字段
- **不存在**：`INSERT`

### 6. 阶段 A 交付物（确认前，必须输出）

1. **字段预览 Markdown 表**（condition_field、中文名、field_type、ext 类型、api_url 建议、parent_id、备注）
2. **待确认 / 疑问清单**（每条说明：不确定点、可选方案 A/B、推荐项）
3. **查重 SQL**（供用户在 dev 执行后反馈是否已有数据）
4. **向用户提出的明确问题**（若无 open question，写「无待确认项，请回复确认后生成 SQL」）

**阶段 A 结束语**：请用户核对预览并回答疑问；**收到确认后再进入阶段 B**。

### 7. 阶段 B 交付物（用户确认后）

1. **ID 分配表**（`START_ID` 来自 `snow_id_gen.py` 或用户指定；连续 +1）
2. **INSERT / UPDATE SQL**（含完整审计字段，见 INSERT 模板）
3. **验收 SQL**

未获用户明确确认前：**不得**进入阶段 B，**不得**输出 INSERT/UPDATE SQL，**不得**声称已执行 SQL。

---

## INSERT 模板

### cfg_query_option

**列（含审计字段）**：

```
id,
bussiness_key, use_type, table_name, table_cn_name, sys_classify,
field_belongs_type, condition_field, condition_field_name,
field_type, value_type, logic, class_path, parent_id,
api_url, request_method, select_label, select_value, select_disabled,
controls, extend_type, is_required,
create_user_id, create_user_name, create_time,
update_user_id, update_user_name, update_time,
version, is_deleted
```

**审计字段默认值**（来自 project-context，用户未覆盖时使用）：

```sql
0, 'system', now(),
0, 'system', now(),
0, false
```

`class_path` 示例：`class com.erp.model.tms.entity.LogisticsBillEntity`

### cfg_query_option_ext

**列（含审计字段）**：

```
id, cfg_query_option_id, type, class_path, data_json,
create_user_id, create_user_name, create_time,
update_user_id, update_user_name, update_time,
version, is_deleted
```

审计字段默认值同上。

### 单行示例

```sql
-- START_ID from snow_id_gen.py: 2050000000000000001
INSERT INTO cfg_query_option (
  id, bussiness_key, use_type, table_name, table_cn_name, sys_classify,
  field_belongs_type, condition_field, condition_field_name,
  field_type, value_type, logic, class_path, parent_id,
  api_url, request_method, select_label, select_value, select_disabled,
  controls, extend_type, is_required,
  create_user_id, create_user_name, create_time,
  update_user_id, update_user_name, update_time,
  version, is_deleted
) VALUES (
  '2050000000000000001', 'logisticsBill', 'allData', 'logistics_bill', '物流单', 'tms',
  'main', 'transportNo', '运输单号',
  'input', 'String', '==', 'class com.erp.model.tms.entity.LogisticsBillEntity', '',
  '', 'get', 'value', 'code', 'disabled',
  '', '', false,
  '0', 'system', now(),
  '0', 'system', now(),
  0, false
);
```

---

## 验收 SQL

```sql
SELECT condition_field, condition_field_name, field_type, parent_id, table_name, api_url
FROM cfg_query_option
WHERE bussiness_key = '{bussinessKey}' AND use_type = '{useType}'
ORDER BY table_name, condition_field;

SELECT o.condition_field, e.type, e.data_json
FROM cfg_query_option o
LEFT JOIN cfg_query_option_ext e ON e.cfg_query_option_id = o.id AND e.is_deleted = false
WHERE o.bussiness_key = '{bussinessKey}';
```

页面验证：三方通知配置字段下拉、规则条件 SpEL 变量、明细子表 Feign 加载。

---

## 样本：logisticsBill / logistics_bill

```
model=tms, tableName=logistics_bill, fieldBelongsType=main,
bussinessKey=logisticsBill, useType=allData
Entity: com.erp.model.tms.entity.LogisticsBillEntity
```

Agent 应读取该 Entity 全字段，跳过 `shopName`（有 `shopId`）等冗余 Name；对 `orderType` / `salesPlatform` 等 String 字段先确认 enum/dict 再定 ext。

**阶段 A 示例**：输出预览表 + 「orderType 用 OrderTypeEnum 还是 salesOrderType 字典？」→ 用户确认后 → 跑 `snow_id_gen.py` → 输出完整 SQL。

---

## 禁止事项

- **未获用户确认前生成 SQL**（见「工作流两阶段」）
- 对不确定的 enum/dict/class / api_url **自行拍板**而不提问
- 不编写/维护 Java 生成器代码
- 不修改 CDC、MQ、`cfg_third_notice` 推送逻辑
- 不自动修改 `.cursor/data/query_option.md`（仅提示用户是否补充）
- 不伪造雪花 ID（必须用脚本或用户给定）
- 生产库 INSERT 必须用户明确授权
- **禁止**对 `parent_id` / `api_url` / `request_method` / `select_*` / `controls` / `extend_type` 写 NULL（见 §项目默认配置）
