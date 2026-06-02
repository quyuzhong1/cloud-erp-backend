# 项目 AI 默认上下文配置

本文件用于 Cursor / AI 在生成 Java 类注释、SQL、配置脚本时读取项目默认上下文。

本文件不是业务需求文档，也不是全局开发规则。
只有在 `.cursor/rules/*.mdc` 明确要求时，才允许读取本文件。

---

## 1. Java 类注释默认作者

| 配置项 | 默认值    |
|---|--------|
| author | system |

使用规则：

- 新增 Java 类时，如果同模块已有类注释风格，优先沿用同模块风格。
- 如果用户明确指定 author，以用户指定值为准。
- 如果没有可参考风格，使用本文件默认 author。
- 修改已有类时，不允许修改原有 author。
- 不允许批量替换历史文件中的 author。

---

## 2. SQL 默认审计字段

仅适用于：

- 初始化数据 SQL
- 配置数据 SQL
- 测试数据 SQL
- 数据修复 SQL
- cfg_query_condition 配置 SQL
- cfg_query_option 配置 SQL

| 字段 | 默认值    |
|---|--------|
| create_user_id | 0      |
| update_user_id | 0      |
| create_user_name | system |
| update_user_name | system    |
| create_time | now()  |
| update_time | now()  |
| version | 0      |
| is_deleted | false  |

使用限制：

- 用户明确指定时，以用户指定值为准。
- 不允许在 Java 业务代码中硬编码这些值。
- 不允许作为业务表 DDL 的字段默认值。
- 仅用于 INSERT / UPDATE 类型的数据脚本。

---

## 3. SQL ID 生成脚本

| 配置项 | 默认值 |
|---|---|
| snow_id_script | tools/ai/snow_id_gen.py |

使用规则：

- 用户未指定主键 ID，且需要生成初始化数据或配置数据时，可以执行该脚本获取起始 ID。
- 多条 INSERT 可以在起始 ID 基础上依次递增。
- 如果脚本不存在或执行失败，必须提示用户手动指定起始 ID。
- 不允许凭空伪造雪花 ID。

---

## 4. 使用限制

- 本文件只保存稳定的项目默认配置。
- 不记录临时需求信息。
- 不记录账号密码、Token、密钥。
- 不作为业务需求来源。
- 不用于覆盖用户明确指令。