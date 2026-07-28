-- =============================================================================
-- 爱亚 / WEGO 产品 SKU：切回旧链路（Mongo → dmp_product_info/dmp_sku_info → MQ）
-- 环境：erp-dmp（以下 ID 取自 test 库，其它环境请先核对 dmp_cfg_input.id / system_id）
-- 依据：
--   1) 极风/谷仓：product 主表 + sku 明细 + Product MQ
--   2) 爱亚开放平台 GLINK_QUERY_ITEM_NOTIFY（2026-07 更新版）+ 方案文档 6.2.2
--      核心字段：sku / name / description / status(Active|Inactive) / barcodeList[{unit,barcode}]
--      注意：6.2.2 表格写「barcode」，真实接口为 barcodeList（与 packagingList 同级）；
--            Handler 解析 barcodeList[].barcode 拼成 skuId → Output → thirdBarcode
--   3) WEGO product.search：sku / name / barcode(string[]) / status(0|1|4)
-- 对照表规则（6.2.2）：a 新增；d 停用已映射→禁用；e 启用已映射忽略；f 停用未匹配→删除；b/c 不做
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 0. 常量说明（test）
-- ---------------------------------------------------------------------------
-- 爱亚 input: 2076940293424305603  system: 2076939607026455998
-- WEGO input: 2063000000000001000  system: 2062019816140877826
-- 原 Feign dmp convert（将改为 product 主表）:
--   爱亚: 2076948134671439321
--   WEGO: 2063000000000001013
-- 新建 sku 明细 convert:
--   爱亚: 2076948134671439401
--   WEGO: 2063000000000001401
-- Output:
--   爱亚: 2076948134671439501
--   WEGO: 2063000000000001501
-- type_id 参考谷仓/纬狮仓库商品 MQ：1801575577567135777（若贵环境商品 MQ type 不同请改）

-- =============================================================================
-- 一、爱亚
-- =============================================================================

-- 1.1 原 OmsSync 行改为 product 主表 convert（对齐极风 order=0）
UPDATE dmp_cfg_input_convert
SET type               = '爱亚产品dmp',
    input_status       = 'dmp',
    storage_name       = 'dmp_product_info',
    convert_class      = 'DmpInputDbConvertDmpHandler',
    unique_field_name  = 'sourceSystem,spuNo,authId',
    fixed_value_json   = '{"sourcePlatform":"aiya","sourceSystem":"aiya"}',
    "order"            = 0,
    disabled           = false,
    update_time        = NOW(),
    update_user_id     = '1',
    update_user_name   = 'system',
    version            = COALESCE(version, 0) + 1
WHERE id = '2076948134671439321'
  AND is_deleted = false;

-- 1.2 新增 sku 明细 convert（order=1，DoNext 挂主表）
INSERT INTO dmp_cfg_input_convert (
    id, main_id, input_status, type, storage_name, convert_class,
    unique_field_name, "order", disabled, fixed_value_json,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES (
    '2076948134671439401',
    '2076940293424305603',
    'dmp',
    '爱亚产品详情dmp',
    'dmp_sku_info',
    'AiyaSkuInfoDmpHandler',
    'mainId,skuNo',
    1,
    false,
    '',
    NOW(), NOW(), '1', 'system', '1', 'system', 0, false
)
ON CONFLICT (id) DO UPDATE SET
    convert_class     = EXCLUDED.convert_class,
    storage_name      = EXCLUDED.storage_name,
    unique_field_name = EXCLUDED.unique_field_name,
    "order"           = EXCLUDED."order",
    disabled          = false,
    is_deleted        = false,
    update_time       = NOW();

-- 1.3 product mapping（原始 Mongo 字段 → dmp_product_info）
-- 先清再插，避免重复执行
UPDATE dmp_cfg_input_convert_mapping
SET is_deleted = true, update_time = NOW()
WHERE main_id = '2076948134671439321'
  AND is_deleted = false;

INSERT INTO dmp_cfg_input_convert_mapping (
    id, main_id, original_key, convert_key, disabled,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES
-- authId 由框架写入 Mongo（id 须 ≤ varchar(19)）
('2076948134671439101', '2076948134671439321', 'authId', 'auth_id',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
-- 6.2.2：仓库sku ← sku；扁平 SKU 合成一条 product
('2076948134671439102', '2076948134671439321', 'sku',    'spu_no',   false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2076948134671439103', '2076948134671439321', 'sku',    'spu_id',   false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
-- 6.2.2：产品名称 ← name（文档标必填；联调偶发无 name 时 sku 侧 Handler 用 description 兜底）
('2076948134671439104', '2076948134671439321', 'name',   'spu_name', false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false);
-- 响应 itemList 无 createdTime/updatedTime（仅请求侧有 From/To），故不映射时间字段

-- 1.4 sku mapping（AiyaSkuInfoDmpHandler.getDetailList 产出键 → dmp_sku_info）
-- Handler：sku→skuNo；name 空则 description→name；barcodeList→skuId；status 原文 Active/Inactive
UPDATE dmp_cfg_input_convert_mapping
SET is_deleted = true, update_time = NOW()
WHERE main_id = '2076948134671439401'
  AND is_deleted = false;

INSERT INTO dmp_cfg_input_convert_mapping (
    id, main_id, original_key, convert_key, disabled,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES
('2076948134671439201', '2076948134671439401', 'skuNo',  'sku_no',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2076948134671439202', '2076948134671439401', 'name',   'name',    false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2076948134671439203', '2076948134671439401', 'skuId',  'sku_id',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2076948134671439204', '2076948134671439401', 'status', 'status',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false);

-- 1.5 Output MQ（挂 sku convert，与极风/谷仓一致）
INSERT INTO dmp_cfg_output (
    id, system_id, input_convert_id, type, type_id, disabled, extend_json,
    output_class, push_rate, app_id, flow_code, flow_name, exec_system, exec_url, bill_type,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES (
    '2076948134671439501',
    '2076939607026455998',
    '2076948134671439401',
    'mq',
    '1801575577567135777',
    false,
    NULL,
    'AiyaProductRocketMQTaskHandler',
    NULL,
    '',
    '',
    '爱亚产品→OMS Listing',
    'dmp',
    '/',
    '',
    NOW(), NOW(), '1', 'system', '1', 'system', 0, false
)
ON CONFLICT (id) DO UPDATE SET
    input_convert_id = EXCLUDED.input_convert_id,
    output_class     = EXCLUDED.output_class,
    system_id        = EXCLUDED.system_id,
    app_id           = EXCLUDED.app_id,
    exec_url         = EXCLUDED.exec_url,
    disabled         = false,
    is_deleted       = false,
    update_time      = NOW();

-- 1.6 Output Detail（按授权 next_level_id；无此行则 MQ 永不创建）
INSERT INTO dmp_cfg_output_detail (
    id, main_id, next_level_id, last_time, next_time,
    interval_time, override_time, max_retry_count, exec_timeout,
    disabled, create_time, update_time, create_user_name, create_user_id,
    update_user_name, update_user_id, version, is_deleted, remark,
    dealy_time, max_interval_time, next_level_type
)
SELECT
    LEFT(MD5(RANDOM()::TEXT || next_level_id), 19),
    '2076948134671439501',
    next_level_id,
    NOW(), NOW(),
    0, 0, 0, 0,
    false, NOW(), NOW(), 'system', '1',
    'system', '1', 0, false, '爱亚产品→OMS',
    0, 0, ''
FROM (
    SELECT DISTINCT d.next_level_id
    FROM dmp_cfg_output_detail d
    JOIN dmp_cfg_output o ON o.id = d.main_id AND o.is_deleted = false
    WHERE o.system_id = '2076939607026455998'
      AND d.is_deleted = false
      AND d.next_level_id <> ''
) auth
WHERE NOT EXISTS (
    SELECT 1 FROM dmp_cfg_output_detail x
    WHERE x.main_id = '2076948134671439501'
      AND x.next_level_id = auth.next_level_id
      AND x.is_deleted = false
);


-- =============================================================================
-- 二、WEGO
-- =============================================================================

-- 2.1 原 OmsSync 行改为 product 主表
UPDATE dmp_cfg_input_convert
SET type               = 'WEGO产品dmp',
    input_status       = 'dmp',
    storage_name       = 'dmp_product_info',
    convert_class      = 'DmpInputDbConvertDmpHandler',
    unique_field_name  = 'sourceSystem,spuNo,authId',
    fixed_value_json   = '{"sourcePlatform":"wego","sourceSystem":"wego"}',
    "order"            = 0,
    disabled           = false,
    update_time        = NOW(),
    update_user_id     = '1',
    update_user_name   = 'system',
    version            = COALESCE(version, 0) + 1
WHERE id = '2063000000000001013'
  AND is_deleted = false;

-- 2.2 新增 sku 明细 convert
INSERT INTO dmp_cfg_input_convert (
    id, main_id, input_status, type, storage_name, convert_class,
    unique_field_name, "order", disabled, fixed_value_json,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES (
    '2063000000000001401',
    '2063000000000001000',
    'dmp',
    'WEGO产品详情dmp',
    'dmp_sku_info',
    'WegoSkuInfoDmpHandler',
    'mainId,skuNo',
    1,
    false,
    '',
    NOW(), NOW(), '1', 'system', '1', 'system', 0, false
)
ON CONFLICT (id) DO UPDATE SET
    convert_class     = EXCLUDED.convert_class,
    storage_name      = EXCLUDED.storage_name,
    unique_field_name = EXCLUDED.unique_field_name,
    "order"           = EXCLUDED."order",
    disabled          = false,
    is_deleted        = false,
    update_time       = NOW();

-- 2.3 product mapping
UPDATE dmp_cfg_input_convert_mapping
SET is_deleted = true, update_time = NOW()
WHERE main_id = '2063000000000001013'
  AND is_deleted = false;

INSERT INTO dmp_cfg_input_convert_mapping (
    id, main_id, original_key, convert_key, disabled,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES
('2063000000000001101', '2063000000000001013', 'authId', 'auth_id',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001102', '2063000000000001013', 'sku',    'spu_no',   false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001103', '2063000000000001013', 'sku',    'spu_id',   false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001104', '2063000000000001013', 'name',   'spu_name', false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false);
-- OpenAPI 响应 list[] 无 createTime/updateTime，不映射时间

-- 2.4 sku mapping（WegoSkuInfoDmpHandler：barcode[]→skuId；status 1/4→Active/Inactive）
UPDATE dmp_cfg_input_convert_mapping
SET is_deleted = true, update_time = NOW()
WHERE main_id = '2063000000000001401'
  AND is_deleted = false;

INSERT INTO dmp_cfg_input_convert_mapping (
    id, main_id, original_key, convert_key, disabled,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES
('2063000000000001201', '2063000000000001401', 'skuNo',  'sku_no',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001202', '2063000000000001401', 'name',   'name',    false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001203', '2063000000000001401', 'skuId',  'sku_id',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false),
('2063000000000001204', '2063000000000001401', 'status', 'status',  false, NOW(), NOW(), '1', 'system', '1', 'system', 0, false);

-- 2.5 Output MQ
INSERT INTO dmp_cfg_output (
    id, system_id, input_convert_id, type, type_id, disabled, extend_json,
    output_class, push_rate, app_id, flow_code, flow_name, exec_system, exec_url, bill_type,
    create_time, update_time, create_user_id, create_user_name,
    update_user_id, update_user_name, version, is_deleted
) VALUES (
    '2063000000000001501',
    '2062019816140877826',
    '2063000000000001401',
    'mq',
    '1801575577567135777',
    false,
    NULL,
    'WegoProductRocketMQTaskHandler',
    NULL,
    '',
    '',
    'WEGO产品→OMS Listing',
    'dmp',
    '/',
    '',
    NOW(), NOW(), '1', 'system', '1', 'system', 0, false
)
ON CONFLICT (id) DO UPDATE SET
    input_convert_id = EXCLUDED.input_convert_id,
    output_class     = EXCLUDED.output_class,
    system_id        = EXCLUDED.system_id,
    app_id           = EXCLUDED.app_id,
    exec_url         = EXCLUDED.exec_url,
    disabled         = false,
    is_deleted       = false,
    update_time      = NOW();

-- 2.6 Output Detail（按授权 next_level_id；无此行则 getDmpCfgOutputDetailEntity 为空，MQ 永不创建）
-- 已有库存/仓等输出详情的 WEGO 授权，自动补一条产品 Output Detail
INSERT INTO dmp_cfg_output_detail (
    id, main_id, next_level_id, last_time, next_time,
    interval_time, override_time, max_retry_count, exec_timeout,
    disabled, create_time, update_time, create_user_name, create_user_id,
    update_user_name, update_user_id, version, is_deleted, remark,
    dealy_time, max_interval_time, next_level_type
)
SELECT
    LEFT(MD5(RANDOM()::TEXT || next_level_id), 19),
    '2063000000000001501',
    next_level_id,
    NOW(), NOW(),
    0, 0, 0, 0,
    false, NOW(), NOW(), 'system', '1',
    'system', '1', 0, false, 'WEGO产品→OMS',
    0, 0, ''
FROM (
    SELECT DISTINCT d.next_level_id
    FROM dmp_cfg_output_detail d
    JOIN dmp_cfg_output o ON o.id = d.main_id AND o.is_deleted = false
    WHERE o.system_id = '2062019816140877826'
      AND d.is_deleted = false
      AND d.next_level_id <> ''
) auth
WHERE NOT EXISTS (
    SELECT 1 FROM dmp_cfg_output_detail x
    WHERE x.main_id = '2063000000000001501'
      AND x.next_level_id = auth.next_level_id
      AND x.is_deleted = false
);


-- =============================================================================
-- 三、执行后核对（只读）
-- =============================================================================
-- SELECT id, type, storage_name, convert_class, unique_field_name, "order", fixed_value_json
-- FROM dmp_cfg_input_convert
-- WHERE main_id IN ('2076940293424305603','2063000000000001000') AND is_deleted=false
-- ORDER BY main_id, "order";
--
-- SELECT main_id, original_key, convert_key
-- FROM dmp_cfg_input_convert_mapping
-- WHERE main_id IN ('2076948134671439321','2076948134671439401','2063000000000001013','2063000000000001401')
--   AND is_deleted=false ORDER BY main_id, convert_key;
--
-- SELECT id, input_convert_id, output_class, system_id, disabled
-- FROM dmp_cfg_output
-- WHERE id IN ('2076948134671439501','2063000000000001501');
--
-- SELECT id, main_id, next_level_id, disabled
-- FROM dmp_cfg_output_detail
-- WHERE main_id IN ('2076948134671439501','2063000000000001501') AND is_deleted=false;

COMMIT;

-- =============================================================================
-- 字段对照摘要（对齐开放平台文档 + 方案 6.2.2 + WEGO OpenAPI）
-- =============================================================================
-- 【爱亚 6.2.2 → 落库/OMS】
-- 仓库sku          ← sku              → sku_no / platformSkuNo
-- 产品名称         ← name(|description)→ name / platformSkuName
-- 平台产品ID/条形码 ← barcodeList[].barcode（方案表写 barcode，接口为 barcodeList）
--                  → sku_id / thirdBarcode
-- status Active/Inactive → dmp_sku_info.status → platformStatus
--   Active：a 新增未匹配(disable) / e 已映射忽略
--   Inactive：d 已映射禁用 / f 未匹配软删；新 Inactive 不落库
-- packagingList / brand / category / 尺寸重量：对照表不需要，不映射
--
-- 【WEGO】
-- sku / name / barcode[] / status(0丢弃,1→Active,4→Inactive) 同上
-- alias、omsLength 等：对照表不需要
-- =============================================================================
