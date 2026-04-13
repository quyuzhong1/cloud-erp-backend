# 高级查询配置自动生成 (Lite)

## 1. 命名与 ID 策略
- **Code 格式**: `{module}:{小驼峰tableName}:paging`。
- **别名逻辑**: 表名首字母组合（如 `qc_standard` -> `qs`），`value` 必须带别名。
- **ID 策略**: 
  1. 优先用户指定起始 ID。
  2. 兜底执行 `antigravity-skills\scripts\snow_id_gen.py` 获取真实 ID。失败则报错停止，严禁编造。

## 2. 核心映射矩阵 (Field Mapping)
| 字段特征 | controls | data_type | 动态检索 / 逻辑 |
| :--- | :--- | :--- | :--- |
| **boolean 类型** | select | boolean | 直接填充 [是否] 静态 JSON |
| **名称含 id/type/status** | select | string | 检索 `query_option_lite.md` 匹配 ID |
| **名称含 person/user/_by** | select | string | 固定 ID `1742885076630179841` |
| **字段名为 disabled** | select | string | 填充 [启用/停用] 静态 JSON |
| **字段名为 invalid_status**| select | string | 填充 [已作废/未作废] 静态 JSON |
| **常规字符/数字** | input | string/number | 不执行检索 |
| **日期类型** | date | date | `date_type` 设为 `date` |

## 3. 特殊逻辑红线 🚨
- **动态维护**: 生成 `cfg_query_option` 脚本后，自动将其 ID/URL/Name 追加至引用库。
- **枚举优先**: 若注释含 `Enum`，无论是否有映射 ID，均执行“枚举处理”逻辑。
- **强制注释**: 使用 `query_option_id` 时，脚本上方必须带 `-- option: {id}, {url}, {name}`。

## 4. 必带默认配置 (必填)
1. **tab列表**: `index: 1000`, `is_extend: true`, `display_type: 'tab'`。
2. **导出ids**: `index: 2000`, `value: {alias}.id`, `display_type: 'export'`。

## 5. SQL 标准模板
```sql
-- option: {id}, {api_url}, {api_name}
INSERT INTO "public"."cfg_query_condition" ("id", "create_user_id", "create_user_name", "create_time", "update_user_id", "update_user_name", "update_time", "version", "is_deleted", "code", "value", "label", "query_option_id", "controls", "data_type", "date_type", "props", "index", "is_extend", "group_name", "display_type", "option_list") VALUES ('{id}', '1838067106149261313', '陈锦辉', now(), '1838067106149261313', '陈锦辉', now(), 0, 'f', '{code}', '{value}', '{label}', '{query_option_id}', '{controls}', '{data_type}', 'date', '{}', {index}, '{is_extend}', 'default', '{display_type}', '{option_list}');