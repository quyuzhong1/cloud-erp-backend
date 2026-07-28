-- =============================================================================
-- 紧急补丁：爱亚/WEGO 产品 Output 缺少 dmp_cfg_output_detail，导致 MQ 永不推 OMS
-- 现象：dmp_sku_info.status=Inactive，但 listing_info.platform_status 仍为 Active，
--       sku_mapping.status 仍为 enable
-- 根因：getDmpCfgOutputDetailEntity 依赖 output_detail(next_level_id=授权id)
-- 环境：erp-dmp test（其它环境改 system_id / output id）
-- =============================================================================

BEGIN;

-- WEGO 产品 Output Detail（output=2063000000000001501）
INSERT INTO dmp_cfg_output_detail (
    id, main_id, next_level_id, last_time, next_time,
    interval_time, override_time, max_retry_count, exec_timeout,
    disabled, create_time, update_time, create_user_name, create_user_id,
    update_user_name, update_user_id, version, is_deleted, remark,
    dealy_time, max_interval_time, next_level_type
)
SELECT
    LEFT(MD5('wego-product-out-' || next_level_id), 19),
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

-- 爱亚 产品 Output Detail（output=2076948134671439501）
INSERT INTO dmp_cfg_output_detail (
    id, main_id, next_level_id, last_time, next_time,
    interval_time, override_time, max_retry_count, exec_timeout,
    disabled, create_time, update_time, create_user_name, create_user_id,
    update_user_name, update_user_id, version, is_deleted, remark,
    dealy_time, max_interval_time, next_level_type
)
SELECT
    LEFT(MD5('aiya-product-out-' || next_level_id), 19),
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

COMMIT;

-- 核对：
-- SELECT id, main_id, next_level_id FROM dmp_cfg_output_detail
-- WHERE main_id IN ('2063000000000001501','2076948134671439501') AND is_deleted=false;

-- OMS 侧历史脏数据可手工纠正（本例 sku 852741 / mapping 2065026117666988033）：
-- UPDATE listing_info SET platform_status='Inactive', update_time=NOW()
-- WHERE id='2065025747230253066' AND platform_sku_no='852741';
-- UPDATE sku_mapping SET status='disable', update_time=NOW(), version=version+1
-- WHERE id='2065026117666988033';
-- 或补 detail 后重新「同步仓库产品」，走 MQ 自动回收。
