-- 迭代 26.7.3 后端 DDL/DML
-- 执行顺序：DDL -> DML 数据迁移 -> cfg_setting 配置

-- ============================================================
-- 1. 售后装箱：箱唛作废状态 / 作废原因 / 作废时间
-- ============================================================
ALTER TABLE "public"."after_sale_pack"
    ADD COLUMN "invalid_status" BOOL NOT NULL DEFAULT FALSE;

ALTER TABLE "public"."after_sale_pack"
    ADD COLUMN "invalid_remark" VARCHAR(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING;

ALTER TABLE "public"."after_sale_pack"
    ADD COLUMN "invalid_time" TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN "public"."after_sale_pack"."invalid_status" IS '作废状态: false=未作废, true=已作废';
COMMENT ON COLUMN "public"."after_sale_pack"."invalid_remark" IS '作废描述';
COMMENT ON COLUMN "public"."after_sale_pack"."invalid_time" IS '作废时间';

-- ============================================================
-- 2. 采购退货：退货原因类型
-- ============================================================
ALTER TABLE "public"."po_return"
    ADD COLUMN "return_reason_type" VARCHAR(32) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING;

COMMENT ON COLUMN "public"."po_return"."return_reason_type" IS '退货原因类型: defect=瑕疵品, other=其它';

-- 历史数据：退货说明含「瑕疵」归为「瑕疵品」，其余归为「其它」
UPDATE "public"."po_return"
SET "return_reason_type" = CASE
        WHEN COALESCE("return_remark", '') LIKE '%瑕疵%' THEN 'defect'
        ELSE 'other'
    END,
    "update_user_id" = '',
    "update_user_name" = '',
    "update_time" = now(),
    "version" = "version" + 1
WHERE "is_deleted" = FALSE
  AND COALESCE("return_reason_type", '') = '';

-- 2.1 采购退货原因类型字典（PDA/PC 下拉：/wms/dict/drop/down?type=poReturnReasonType）
-- value 与 PoReturnReasonTypeEnum / po_return.return_reason_type 保持一致
-- 依赖：PostgreSQL 函数 snow_next_id() 须已存在。
INSERT INTO "public"."dict_basic" (
    "id",
    "create_user_id",
    "create_user_name",
    "create_time",
    "update_user_id",
    "update_user_name",
    "update_time",
    "version",
    "is_deleted",
    "remark",
    "value",
    "type",
    "name",
    "status",
    "sort",
    "type_name"
)
SELECT
    snow_next_id(),
    '',
    '',
    now(),
    '',
    '',
    now(),
    0,
    FALSE,
    '',
    'defect',
    'poReturnReasonType',
    '瑕疵品',
    TRUE,
    1,
    '采购退货原因类型'
WHERE NOT EXISTS (
    SELECT 1
    FROM "public"."dict_basic" AS d
    WHERE d."type" = 'poReturnReasonType'
      AND d."value" = 'defect'
      AND d."is_deleted" = FALSE
);

INSERT INTO "public"."dict_basic" (
    "id",
    "create_user_id",
    "create_user_name",
    "create_time",
    "update_user_id",
    "update_user_name",
    "update_time",
    "version",
    "is_deleted",
    "remark",
    "value",
    "type",
    "name",
    "status",
    "sort",
    "type_name"
)
SELECT
    snow_next_id(),
    '',
    '',
    now(),
    '',
    '',
    now(),
    0,
    FALSE,
    '',
    'other',
    'poReturnReasonType',
    '其它',
    TRUE,
    2,
    '采购退货原因类型'
WHERE NOT EXISTS (
    SELECT 1
    FROM "public"."dict_basic" AS d
    WHERE d."type" = 'poReturnReasonType'
      AND d."value" = 'other'
      AND d."is_deleted" = FALSE
);

-- ============================================================
-- 3. 寄修单：状态迁移（toBeShipped 拆分为 待商家寄出 + 已完成）
-- ============================================================

-- 3.1 已有商家寄出单号的旧单，主表状态迁移为 completed
UPDATE "public"."after_sale" AS a
SET "status" = 'completed',
    "update_user_id" = '',
    "update_user_name" = '',
    "update_time" = now(),
    "version" = a."version" + 1
FROM "public"."after_sale_progress" AS p
WHERE a."id" = p."main_id"
  AND p."node" = 'toBeShipped'
  AND COALESCE(p."track_no", '') <> ''
  AND a."status" = 'toBeShipped'
  AND a."is_deleted" = FALSE
  AND p."is_deleted" = FALSE;

-- 3.2 补全 completed 进度节点（index=6，与 cfg_setting 节点配置一致）
-- 依赖：PostgreSQL 函数 snow_next_id() 须已存在。
INSERT INTO "public"."after_sale_progress" (
    "id",
    "create_user_id",
    "create_user_name",
    "create_time",
    "update_user_id",
    "update_user_name",
    "update_time",
    "version",
    "is_deleted",
    "remark",
    "main_id",
    "node",
    "node_time",
    "track_no",
    "index"
)
SELECT
    snow_next_id(),
    '',
    '',
    now(),
    '',
    '',
    now(),
    0,
    FALSE,
    '',
    a."id",
    'completed',
    COALESCE(sp."node_time", now()),
    '',
    6
FROM "public"."after_sale" AS a
LEFT JOIN "public"."after_sale_progress" AS sp
    ON sp."main_id" = a."id"
   AND sp."node" = 'toBeShipped'
   AND sp."is_deleted" = FALSE
WHERE a."is_deleted" = FALSE
  AND a."status" = 'completed'
  AND NOT EXISTS (
        SELECT 1
        FROM "public"."after_sale_progress" AS cp
        WHERE cp."main_id" = a."id"
          AND cp."node" = 'completed'
          AND cp."is_deleted" = FALSE
    );

-- 3.3 已完成节点写入 node_time（关联 toBeShipped 节点时间）
UPDATE "public"."after_sale_progress" AS cp
SET "node_time" = sp."node_time",
    "update_user_id" = '',
    "update_user_name" = '',
    "update_time" = now(),
    "version" = cp."version" + 1
FROM "public"."after_sale_progress" AS sp
INNER JOIN "public"."after_sale" AS a ON a."id" = cp."main_id"
WHERE cp."main_id" = sp."main_id"
  AND cp."node" = 'completed'
  AND sp."node" = 'toBeShipped'
  AND cp."is_deleted" = FALSE
  AND sp."is_deleted" = FALSE
  AND a."status" = 'completed'
  AND a."is_deleted" = FALSE
  AND (cp."node_time" IS NULL OR cp."node_time" < sp."node_time");

-- ============================================================
-- 4. 寄修单：售后维修节点配置（cfg_setting.afterSaleNode）
-- ============================================================
UPDATE "public"."cfg_setting"
SET "value" = '{"nodeList":[{"index":1,"node":"approveIng","nodeName":"客服审核"},{"index":2,"node":"toBeReturned","nodeName":"待客户寄件"},{"index":3,"node":"afterSalesReceived","nodeName":"待售后签收"},{"index":4,"node":"repair","nodeName":"检测/维修中"},{"index":5,"node":"toBeShipped","nodeName":"待商家寄出"},{"index":6,"node":"completed","nodeName":"已完成"}]}',
    "update_user_id" = '',
    "update_user_name" = '',
    "update_time" = now(),
    "version" = "version" + 1
WHERE "key" = 'afterSaleNode'
  AND "is_deleted" = FALSE;

-- 若不存在则插入（按实际 cfg_setting 表结构补充 module/type 字段后再执行）
-- INSERT INTO "public"."cfg_setting" (...) SELECT ... WHERE NOT EXISTS (...);

-- ============================================================
-- 5. 采购退货：PDA 默认采购组织（cfg_setting.poReturnDefaultOrg）
-- 说明：purchaseOrgId 按核算组织名称初始化；若目标环境无「东莞市简拍智造科技有限公司」则为 null，需在系统配置页手工维护。
-- 依赖：PostgreSQL 函数 snow_next_id() 须已存在。
-- ============================================================
INSERT INTO "public"."dict_basic" (
    "id",
    "create_user_id",
    "create_user_name",
    "create_time",
    "update_user_id",
    "update_user_name",
    "update_time",
    "version",
    "is_deleted",
    "remark",
    "value",
    "type",
    "name",
    "status",
    "sort",
    "type_name"
)
SELECT
    snow_next_id(),
    '',
    '',
    now(),
    '',
    '',
    now(),
    0,
    FALSE,
    '',
    'poReturnDefaultOrg',
    'cfgSetting',
    '采购退货默认采购组织',
    TRUE,
    12,
    '系统配置'
WHERE NOT EXISTS (
    SELECT 1
    FROM "public"."dict_basic" AS d
    WHERE d."type" = 'cfgSetting'
      AND d."value" = 'poReturnDefaultOrg'
      AND d."is_deleted" = FALSE
);

INSERT INTO "public"."cfg_setting" (
    "id",
    "create_user_id",
    "create_user_name",
    "create_time",
    "update_user_id",
    "update_user_name",
    "update_time",
    "version",
    "is_deleted",
    "remark",
    "key",
    "data_json",
    "disabled",
    "index"
)
SELECT
    snow_next_id(),
    '',
    '',
    now(),
    '',
    '',
    now(),
    0,
    FALSE,
    '',
    'poReturnDefaultOrg',
    jsonb_build_object(
        'purchaseOrgId',
        (
            SELECT c."id"
            FROM "public"."sys_accounting_company" AS c
            WHERE c."company_name" = '东莞市简拍智造科技有限公司'
              AND c."is_deleted" = FALSE
            LIMIT 1
        )
    ),
    FALSE,
    12
WHERE NOT EXISTS (
    SELECT 1
    FROM "public"."cfg_setting" AS s
    WHERE s."key" = 'poReturnDefaultOrg'
      AND s."is_deleted" = FALSE
);

-- ============================================================
-- 6. 寄修单：商家寄出物流轨迹状态（Job 同步 Track123，列表展示）
-- ============================================================
ALTER TABLE "public"."after_sale"
    ADD COLUMN IF NOT EXISTS "outbound_track_status" VARCHAR(64) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::CHARACTER VARYING;

COMMENT ON COLUMN "public"."after_sale"."outbound_track_status" IS '商家寄出物流轨迹状态code（Track123，列表「商家寄件物流状态」）';
