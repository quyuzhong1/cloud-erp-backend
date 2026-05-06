# Skill 索引中心

## 📌 任务路由逻辑
1. **数据库任务 (DDL/DML)**: 加载 `core/constants.md` + `database/doc/pgsql.md`。
2. **后端开发 (CRUD/异常)**: 加载 `core/constants.md` + `backend/doc/api_logic.md` + `backend/exceptions_lite.md`。
3. **自动化配置 (高级查询)**: 仅在生成配置 SQL 时加载 `automation/query_gen_lite.md`，并在需要下拉框时检索 `data/query_option_lite.md`。

## 🚨 全局三原色 (所有任务必读)
* **署名**: 新建或大幅重构的 Java 类作者统一为 `jack`，已有类小改不强制改历史署名。
* **唯一性**: 严禁重复生成、严禁修改历史数据（错误码/ID）。
* **非空**: 所有字段必须 `NOT NULL` 且带 `COMMENT`。