# Database Field Naming Examples

## Example 1: Field Naming

Input:

```text
采购人, 采购时间, 采购发票审批时间, 供应商, 结算方式
```

Output:

| 中文字段 | 推荐字段名 | 类型 | 默认/约束 | 说明 |
|---|---|---|---|---|
| 采购人 | po_buyer_id | VARCHAR(32) | NOT NULL DEFAULT '' | 采购人ID |
| 采购时间 | po_time | TIMESTAMP | NOT NULL DEFAULT now() | 采购下单时间 |
| 采购发票审批时间 | fin_invoice_approve_time | TIMESTAMP | NOT NULL DEFAULT now() | 采购发票审批时间 |
| 供应商 | supplier_id | VARCHAR(32) | NOT NULL DEFAULT '' | 供应商ID |
| 结算方式 | settle_method | VARCHAR(50) | NOT NULL DEFAULT '' | 结算方式 |

## Example 2: Purchase Order Table

Input:

```text
采购订单表
```

Output:

```sql
CREATE TABLE po_order (
  id VARCHAR(32) PRIMARY KEY,
  create_user_id VARCHAR(32) NOT NULL DEFAULT '',
  create_user_name VARCHAR(64) NOT NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT now(),
  update_user_id VARCHAR(32) NOT NULL DEFAULT '',
  update_user_name VARCHAR(64) NOT NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT now(),
  version INTEGER NOT NULL DEFAULT 0,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

  po_code VARCHAR(64) NOT NULL DEFAULT '',
  po_time TIMESTAMP NOT NULL DEFAULT now(),
  po_buyer_id VARCHAR(32) NOT NULL DEFAULT '',
  supplier_id VARCHAR(32) NOT NULL DEFAULT '',
  settle_method VARCHAR(50) NOT NULL DEFAULT '',
  po_status VARCHAR(50) NOT NULL DEFAULT 'draft',
  remark TEXT NOT NULL DEFAULT ''
);

COMMENT ON TABLE po_order IS '采购订单表';
COMMENT ON COLUMN po_order.id IS '主键';
COMMENT ON COLUMN po_order.create_user_id IS '创建人ID';
COMMENT ON COLUMN po_order.create_user_name IS '创建人名称';
COMMENT ON COLUMN po_order.create_time IS '创建时间';
COMMENT ON COLUMN po_order.update_user_id IS '修改人ID';
COMMENT ON COLUMN po_order.update_user_name IS '修改人名称';
COMMENT ON COLUMN po_order.update_time IS '修改时间';
COMMENT ON COLUMN po_order.version IS '乐观锁版本号';
COMMENT ON COLUMN po_order.is_deleted IS '是否逻辑删除';
COMMENT ON COLUMN po_order.po_code IS '采购订单编号';
COMMENT ON COLUMN po_order.po_time IS '采购下单时间';
COMMENT ON COLUMN po_order.po_buyer_id IS '采购人ID';
COMMENT ON COLUMN po_order.supplier_id IS '供应商ID';
COMMENT ON COLUMN po_order.settle_method IS '结算方式';
COMMENT ON COLUMN po_order.po_status IS '采购订单状态：draft=草稿,submitted=已提交,approved=已审核,cancelled=已取消';
COMMENT ON COLUMN po_order.remark IS '备注说明';
```

## Example 3: Explicit Table With Fields

Input:

```text
物流渠道表：物流商, 渠道编码, 渠道名称, 计费方式, 是否启用
```

Output:

```sql
CREATE TABLE log_channel (
  id VARCHAR(32) PRIMARY KEY,
  create_user_id VARCHAR(32) NOT NULL DEFAULT '',
  create_user_name VARCHAR(64) NOT NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT now(),
  update_user_id VARCHAR(32) NOT NULL DEFAULT '',
  update_user_name VARCHAR(64) NOT NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT now(),
  version INTEGER NOT NULL DEFAULT 0,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

  log_supplier_id VARCHAR(32) NOT NULL DEFAULT '',
  channel_code VARCHAR(64) NOT NULL DEFAULT '',
  channel_name VARCHAR(128) NOT NULL DEFAULT '',
  billing_method VARCHAR(50) NOT NULL DEFAULT '',
  status BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE log_channel IS '物流渠道表';
COMMENT ON COLUMN log_channel.id IS '主键';
COMMENT ON COLUMN log_channel.create_user_id IS '创建人ID';
COMMENT ON COLUMN log_channel.create_user_name IS '创建人名称';
COMMENT ON COLUMN log_channel.create_time IS '创建时间';
COMMENT ON COLUMN log_channel.update_user_id IS '修改人ID';
COMMENT ON COLUMN log_channel.update_user_name IS '修改人名称';
COMMENT ON COLUMN log_channel.update_time IS '修改时间';
COMMENT ON COLUMN log_channel.version IS '乐观锁版本号';
COMMENT ON COLUMN log_channel.is_deleted IS '是否逻辑删除';
COMMENT ON COLUMN log_channel.log_supplier_id IS '物流商ID';
COMMENT ON COLUMN log_channel.channel_code IS '渠道编码';
COMMENT ON COLUMN log_channel.channel_name IS '渠道名称';
COMMENT ON COLUMN log_channel.billing_method IS '计费方式';
COMMENT ON COLUMN log_channel.status IS '是否启用';
```
