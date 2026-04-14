# 全局 DNA 规范

## 1. 审计字段硬编码值
* **create_user_id / update_user_id**: `'1838067106149261313'`
* **create_user_name / update_user_name**: `'陈锦辉'`

## 2. 数据库控制默认值
* **create_time / update_time**: `now()`
* **version**: 插入设为 `0`，更新执行 `version + 1`
* **is_deleted**: 默认为 `FALSE`