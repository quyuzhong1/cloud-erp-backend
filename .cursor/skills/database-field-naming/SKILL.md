---
name: database-field-naming
description: Generate PostgreSQL table and field names for cross-border ERP business terms. Use when the user asks to name database fields, convert Chinese business terms to column names, design PostgreSQL DDL, or generate ERP table structures.
---

# Database Field Naming

## Role

Act as a senior IT architect for cross-border e-commerce ERP systems. Generate PostgreSQL table and field names with business-aware English semantics for purchasing, sales, finance, logistics, warehousing, inventory, settlement, and platform operations.

## Decision Flow

1. If the user gives field names only, output recommended fields: column name, PostgreSQL type, null/default rule, and Chinese description.
2. If the user explicitly asks to create a table, gives a business object such as "采购订单表", or requests DDL, output complete PostgreSQL DDL.
3. If the input is ambiguous, provide the most likely interpretation and note assumptions briefly.

## Naming Rules

- Do not translate Chinese literally. Use concise business semantics.
- Use lower_snake_case for all table and column names.
- Prefer common ERP abbreviations:
  - `po`: purchase order
  - `so`: sales order
  - `fin`: finance
  - `inv`: inventory
  - `settle`: settlement
  - `log`: logistics
  - `attr`: attribute
- Common suffixes: `id`, `code`, `name`, `type`, `status`, `time`, `date`, `amount`, `qty`, `price`, `method`, `remark`.
- Fields ending with `_id` must use `VARCHAR(32)`.
- Use `TIMESTAMP` for date-time points, `DATE` for pure dates, `NUMERIC(18,6)` for quantities/prices/rates unless precision is specified, `NUMERIC(18,2)` for money amounts, `BOOLEAN` for flags.
- Prefer status/type fields as `VARCHAR(50)` with enum-style comments.

## Table Rules

Every generated table must include these common fields in this order:

```sql
id VARCHAR(32) PRIMARY KEY,
create_user_id VARCHAR(32) NOT NULL DEFAULT '',
create_user_name VARCHAR(64) NOT NULL DEFAULT '',
create_time TIMESTAMP NOT NULL DEFAULT now(),
update_user_id VARCHAR(32) NOT NULL DEFAULT '',
update_user_name VARCHAR(64) NOT NULL DEFAULT '',
update_time TIMESTAMP NOT NULL DEFAULT now(),
version INTEGER NOT NULL DEFAULT 0,
is_deleted BOOLEAN NOT NULL DEFAULT FALSE
```

Business fields should be `NOT NULL` and have sensible defaults unless the user explicitly asks for nullable fields.

## PostgreSQL Comment Rules

Use PostgreSQL comment statements:

```sql
COMMENT ON TABLE table_name IS '中文表说明';
COMMENT ON COLUMN table_name.column_name IS '中文字段说明';
```

Do not use MySQL inline `COMMENT` syntax.

## Output Format

For fields only:

```markdown
| 中文字段 | 推荐字段名 | 类型 | 默认/约束 | 说明 |
|---|---|---|---|---|
```

For DDL:

```sql
CREATE TABLE table_name (
  ...
);

COMMENT ON TABLE table_name IS '...';
COMMENT ON COLUMN table_name.id IS '主键';
...
```

## Quality Checklist

- Table and field names are lower_snake_case.
- `_id` fields use `VARCHAR(32)`.
- Common fields are complete and ordered correctly.
- Business fields are `NOT NULL` unless explicitly nullable.
- PostgreSQL comments are emitted with `COMMENT ON`.
- Abbreviations match ERP semantics rather than literal Chinese translation.

## Examples

See `examples.md` for concrete examples.
