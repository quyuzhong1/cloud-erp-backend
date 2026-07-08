-- =============================================================================
-- 越南盾(VND)兑人民币汇率 - 历史数据修复脚本
-- =============================================================================
-- 适用场景：小数精度升级（NUMERIC 18,6）后，修复历史错误汇率及关联本位币字段
-- 影响范围：3 个单据 / 20 个业务字段（见下方清单）
-- 前置条件：
--   1. 数据库 numeric 字段精度升级（ALTER）已执行完毕
--   2. 业务方已提供「按月修复汇率」数据
-- 筛选条件：原币 currency = 'VND'
-- 匹配规则：按单据 bill_date 所在月份（YYYY-MM）匹配修复汇率
--
-- 执行顺序：
--   Step 0  创建并填入修复汇率表
--   Step 1  备份受影响数据
--   Step 2  预览（确认无误后再执行 Step 3~5）
--   Step 3  WMS 销售出库单
--   Step 4  WMS 退货入库单
--   Step 5  OMS B2C 销售订单
--   Step 6  修复后检查
--
-- 回滚：使用 Step 1 创建的 bak_* 表恢复（见文末回滚脚本）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 影响字段清单
-- -----------------------------------------------------------------------------
-- [WMS 销售出库单] so_outstock + so_outstock_detail
--   - exchange_rate              按月取修复汇率
--   - all_amount_local_currency  = tax_amount * 新汇率
--   - 销售单价(本位币)、含税单价(本位币) 为列表计算字段，无存储列，修复汇率后自动正确
--
-- [WMS 退货入库单] so_return_instock + so_return_instock_detail
--   - exchange_rate                      按月取修复汇率
--   - return_amount_local_currency       = return_amount * 新汇率
--   - tax_return_amount_local_currency   = tax_return_amount * 新汇率
--
-- [OMS B2C 销售订单] so_b2c + so_b2c_detail + so_b2c_finance
--   - so_b2c.exchange_rate / so_b2c_detail.exchange_rate  按月取修复汇率
--   - so_b2c_detail.tax_cost                 含税成本CNY，比例法修复
--   - so_b2c_detail.product_cost              材料成本，比例法修复
--   - so_b2c_detail.first_mile_shipping_cost 头程运费，比例法修复
--   - so_b2c_detail.clearance_customs_tax     清关税费，比例法修复
--   - so_b2c_finance 运费收入/商品成本/物流成本/平台费/转账费/包装辅料费/VAT税费
--     Excel 标注「仅处理CNY字段」：VND 订单在库中 currency='VND' 存原币，CNY 展示由汇率换算；
--     修复主表 exchange_rate 后展示自动正确，**不更新** so_b2c_finance 原币字段（见 Step 5.4 说明）
--
-- 比例法说明：已存储的 CNY 本位币金额按 旧值 * 新汇率 / 旧汇率 重算，
--             等价于 原币金额 * 新汇率（在原币金额不变的前提下）。
-- -----------------------------------------------------------------------------


-- =============================================================================
-- Step 0: 修复汇率表（业务填入实际汇率）
-- =============================================================================
DROP TABLE IF EXISTS tmp_fix_vnd_cny_rate;

CREATE TABLE tmp_fix_vnd_cny_rate (
    rate_month  VARCHAR(7)      NOT NULL PRIMARY KEY,  -- 格式 YYYY-MM
    fix_rate    NUMERIC(18, 6)  NOT NULL,              -- 越南盾兑人民币修复汇率
    remark      VARCHAR(200)
);

COMMENT ON TABLE tmp_fix_vnd_cny_rate IS '越南盾兑人民币历史修复汇率（按月）';
COMMENT ON COLUMN tmp_fix_vnd_cny_rate.rate_month IS '汇率生效月份 YYYY-MM';
COMMENT ON COLUMN tmp_fix_vnd_cny_rate.fix_rate IS '越南盾兑人民币修复汇率';

-- >>> 在此处 INSERT 业务提供的修复汇率（示例，请替换为真实值） <<<
-- INSERT INTO tmp_fix_vnd_cny_rate (rate_month, fix_rate, remark) VALUES
-- ('2024-01', 0.000285000, '2024年1月修复汇率'),
-- ('2024-02', 0.000290000, '2024年2月修复汇率');
-- ... 补全所有涉及月份


-- =============================================================================
-- Step 1: 备份（必须在 UPDATE 之前执行）
-- =============================================================================
DROP TABLE IF EXISTS bak_so_outstock_detail_vnd_fix;
CREATE TABLE bak_so_outstock_detail_vnd_fix AS
SELECT sod.*
FROM so_outstock_detail sod
INNER JOIN so_outstock so ON so.id = sod.main_id AND so.is_deleted = FALSE
WHERE sod.is_deleted = FALSE
  AND sod.currency = 'VND';

DROP TABLE IF EXISTS bak_so_return_instock_detail_vnd_fix;
CREATE TABLE bak_so_return_instock_detail_vnd_fix AS
SELECT srid.*
FROM so_return_instock_detail srid
INNER JOIN so_return_instock sri ON sri.id = srid.main_id AND sri.is_deleted = FALSE
WHERE srid.is_deleted = FALSE
  AND srid.currency = 'VND';

DROP TABLE IF EXISTS bak_so_b2c_vnd_fix;
CREATE TABLE bak_so_b2c_vnd_fix AS
SELECT *
FROM so_b2c
WHERE is_deleted = FALSE
  AND currency = 'VND';

DROP TABLE IF EXISTS bak_so_b2c_detail_vnd_fix;
CREATE TABLE bak_so_b2c_detail_vnd_fix AS
SELECT sbd.*
FROM so_b2c_detail sbd
INNER JOIN so_b2c sb ON sb.id = sbd.main_id AND sb.is_deleted = FALSE
WHERE sbd.is_deleted = FALSE
  AND sb.currency = 'VND';

DROP TABLE IF EXISTS bak_so_b2c_finance_vnd_fix;
CREATE TABLE bak_so_b2c_finance_vnd_fix AS
SELECT sbf.*
FROM so_b2c_finance sbf
INNER JOIN so_b2c sb ON sb.id = sbf.main_id AND sb.is_deleted = FALSE
WHERE sbf.is_deleted = FALSE
  AND sb.currency = 'VND';


-- =============================================================================
-- Step 2: 预览（仅查询，确认影响范围后再执行 Step 3~5）
-- =============================================================================

-- 2.1 检查有无 VND 单据匹配不到修复汇率（有结果则需补全 tmp_fix_vnd_cny_rate）
SELECT 'so_outstock' AS doc_type, so.code, so.bill_date, TO_CHAR(so.bill_date, 'YYYY-MM') AS rate_month
FROM so_outstock so
INNER JOIN so_outstock_detail sod ON sod.main_id = so.id AND sod.currency = 'VND' AND sod.is_deleted = FALSE
LEFT JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(so.bill_date, 'YYYY-MM')
WHERE so.is_deleted = FALSE
  AND r.rate_month IS NULL
UNION ALL
SELECT 'so_return_instock', sri.code, sri.bill_date, TO_CHAR(sri.bill_date, 'YYYY-MM')
FROM so_return_instock sri
INNER JOIN so_return_instock_detail srid ON srid.main_id = sri.id AND srid.currency = 'VND' AND srid.is_deleted = FALSE
LEFT JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(sri.bill_date, 'YYYY-MM')
WHERE sri.is_deleted = FALSE
  AND r.rate_month IS NULL
UNION ALL
SELECT 'so_b2c', sb.code, sb.bill_date, TO_CHAR(sb.bill_date, 'YYYY-MM')
FROM so_b2c sb
LEFT JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(sb.bill_date, 'YYYY-MM')
WHERE sb.is_deleted = FALSE
  AND sb.currency = 'VND'
  AND r.rate_month IS NULL;

-- 2.2 WMS 销售出库单预览
SELECT
    so.code,
    sod.id AS detail_id,
    so.bill_date,
    TO_CHAR(so.bill_date, 'YYYY-MM') AS rate_month,
    sod.exchange_rate AS old_rate,
    r.fix_rate AS new_rate,
    sod.tax_amount,
    sod.all_amount_local_currency AS old_all_amount_lc,
    ROUND(sod.tax_amount * r.fix_rate, 6) AS new_all_amount_lc
FROM so_outstock_detail sod
INNER JOIN so_outstock so ON so.id = sod.main_id AND so.is_deleted = FALSE
INNER JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(so.bill_date, 'YYYY-MM')
WHERE sod.is_deleted = FALSE
  AND sod.currency = 'VND'
  AND sod.exchange_rate IS DISTINCT FROM r.fix_rate
ORDER BY so.bill_date, so.code;

-- 2.3 WMS 退货入库单预览
SELECT
    sri.code,
    srid.id AS detail_id,
    sri.bill_date,
    srid.exchange_rate AS old_rate,
    r.fix_rate AS new_rate,
    srid.return_amount,
    srid.return_amount_local_currency AS old_return_lc,
    ROUND(srid.return_amount * r.fix_rate, 6) AS new_return_lc,
    srid.tax_return_amount,
    srid.tax_return_amount_local_currency AS old_tax_return_lc,
    ROUND(srid.tax_return_amount * r.fix_rate, 6) AS new_tax_return_lc
FROM so_return_instock_detail srid
INNER JOIN so_return_instock sri ON sri.id = srid.main_id AND sri.is_deleted = FALSE
INNER JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(sri.bill_date, 'YYYY-MM')
WHERE srid.is_deleted = FALSE
  AND srid.currency = 'VND'
  AND srid.exchange_rate IS DISTINCT FROM r.fix_rate
ORDER BY sri.bill_date, sri.code;

-- 2.4 OMS B2C 主表预览
SELECT
    sb.code,
    sb.id,
    sb.bill_date,
    sb.exchange_rate AS old_rate,
    r.fix_rate AS new_rate
FROM so_b2c sb
INNER JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(sb.bill_date, 'YYYY-MM')
WHERE sb.is_deleted = FALSE
  AND sb.currency = 'VND'
  AND sb.exchange_rate IS DISTINCT FROM r.fix_rate
ORDER BY sb.bill_date, sb.code;


-- =============================================================================
-- Step 3: WMS 销售出库单修复
-- =============================================================================
BEGIN;

-- 3.1 更新汇率
UPDATE so_outstock_detail sod
SET
    exchange_rate = r.fix_rate,
    update_time   = NOW()
FROM so_outstock so
INNER JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(so.bill_date, 'YYYY-MM')
WHERE sod.main_id = so.id
  AND sod.is_deleted = FALSE
  AND so.is_deleted = FALSE
  AND sod.currency = 'VND'
  AND sod.exchange_rate IS DISTINCT FROM r.fix_rate;

-- 3.2 重算价税合计(本位币) = tax_amount * 新汇率
UPDATE so_outstock_detail sod
SET
    all_amount_local_currency = ROUND(sod.tax_amount * sod.exchange_rate, 6),
    update_time               = NOW()
FROM so_outstock so
WHERE sod.main_id = so.id
  AND sod.is_deleted = FALSE
  AND so.is_deleted = FALSE
  AND sod.currency = 'VND'
  AND sod.tax_amount IS NOT NULL
  AND sod.exchange_rate IS NOT NULL
  AND sod.exchange_rate > 0;

COMMIT;


-- =============================================================================
-- Step 4: WMS 退货入库单修复
-- =============================================================================
BEGIN;

-- 4.1 更新汇率
UPDATE so_return_instock_detail srid
SET
    exchange_rate = r.fix_rate,
    update_time   = NOW()
FROM so_return_instock sri
INNER JOIN tmp_fix_vnd_cny_rate r ON r.rate_month = TO_CHAR(sri.bill_date, 'YYYY-MM')
WHERE srid.main_id = sri.id
  AND srid.is_deleted = FALSE
  AND sri.is_deleted = FALSE
  AND srid.currency = 'VND'
  AND srid.exchange_rate IS DISTINCT FROM r.fix_rate;

-- 4.2 重算本位币退货金额
UPDATE so_return_instock_detail srid
SET
    return_amount_local_currency     = ROUND(srid.return_amount * srid.exchange_rate, 6),
    tax_return_amount_local_currency = ROUND(srid.tax_return_amount * srid.exchange_rate, 6),
    update_time                      = NOW()
FROM so_return_instock sri
WHERE srid.main_id = sri.id
  AND srid.is_deleted = FALSE
  AND sri.is_deleted = FALSE
  AND srid.currency = 'VND'
  AND srid.exchange_rate IS NOT NULL
  AND srid.exchange_rate > 0;

COMMIT;


-- =============================================================================
-- Step 5: OMS B2C 销售订单修复
-- =============================================================================
BEGIN;

-- 5.1 更新主表汇率
UPDATE so_b2c sb
SET
    exchange_rate = r.fix_rate,
    update_time   = NOW()
FROM tmp_fix_vnd_cny_rate r
WHERE r.rate_month = TO_CHAR(sb.bill_date, 'YYYY-MM')
  AND sb.is_deleted = FALSE
  AND sb.currency = 'VND'
  AND sb.exchange_rate IS DISTINCT FROM r.fix_rate;

-- 5.2 同步明细表汇率（与主表保持一致）
UPDATE so_b2c_detail sbd
SET
    exchange_rate = sb.exchange_rate,
    update_time   = NOW()
FROM so_b2c sb
WHERE sbd.main_id = sb.id
  AND sbd.is_deleted = FALSE
  AND sb.is_deleted = FALSE
  AND sb.currency = 'VND';

-- 5.3 重算明细表本位币成本字段（比例法：旧CNY值 * 新汇率 / 旧汇率）
UPDATE so_b2c_detail sbd
SET
    tax_cost                 = CASE
                                   WHEN bak.tax_cost IS NOT NULL
                                        AND bak_main.exchange_rate IS NOT NULL
                                        AND bak_main.exchange_rate > 0
                                       THEN ROUND(bak.tax_cost * sb.exchange_rate / bak_main.exchange_rate, 6)
                               END,
    product_cost             = CASE
                                   WHEN bak.product_cost IS NOT NULL
                                        AND bak_main.exchange_rate IS NOT NULL
                                        AND bak_main.exchange_rate > 0
                                       THEN ROUND(bak.product_cost * sb.exchange_rate / bak_main.exchange_rate, 6)
                               END,
    first_mile_shipping_cost = CASE
                                   WHEN bak.first_mile_shipping_cost IS NOT NULL
                                        AND bak_main.exchange_rate IS NOT NULL
                                        AND bak_main.exchange_rate > 0
                                       THEN ROUND(bak.first_mile_shipping_cost * sb.exchange_rate / bak_main.exchange_rate, 6)
                               END,
    clearance_customs_tax    = CASE
                                   WHEN bak.clearance_customs_tax IS NOT NULL
                                        AND bak_main.exchange_rate IS NOT NULL
                                        AND bak_main.exchange_rate > 0
                                       THEN ROUND(bak.clearance_customs_tax * sb.exchange_rate / bak_main.exchange_rate, 6)
                               END,
    update_time              = NOW()
FROM so_b2c sb
INNER JOIN bak_so_b2c_vnd_fix bak_main ON bak_main.id = sb.id
INNER JOIN bak_so_b2c_detail_vnd_fix bak ON bak.id = sbd.id
WHERE sbd.main_id = sb.id
  AND sbd.is_deleted = FALSE
  AND sb.is_deleted = FALSE
  AND sb.currency = 'VND';

-- 5.4 【不执行 UPDATE】财务表原币字段说明
-- Excel 中运费收入/商品成本/物流成本/平台费/转账费/包装辅料费/VAT税费 均标注「仅处理CNY字段」。
-- 代码逻辑（SoB2cServiceImpl#getFinancialInfo）：VND 订单 so_b2c_finance.currency='VND'，费用存原币；
-- CNY 展示 = 原币 * exchange_rate，故 Step 5.1 修复主表汇率后自动生效，无需改写 finance 表。
--
-- 若存在极少数 main.currency='VND' 且 finance.currency='CNY' 的异常数据，可人工核对后执行下方可选脚本：
--
-- UPDATE so_b2c_finance sbf
-- SET
--     shipping_cost    = ROUND(bak.shipping_cost    * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     item_cost        = ROUND(bak.item_cost        * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     logistics_cost   = ROUND(bak.logistics_cost   * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     platform_cost    = ROUND(bak.platform_cost    * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     transfer_cost    = ROUND(bak.transfer_cost    * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     accessories_cost = ROUND(bak.accessories_cost * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     vat_cost         = ROUND(bak.vat_cost         * sb.exchange_rate / NULLIF(bak_main.exchange_rate, 0), 6),
--     update_time      = NOW()
-- FROM so_b2c sb
-- INNER JOIN bak_so_b2c_vnd_fix bak_main ON bak_main.id = sb.id
-- INNER JOIN bak_so_b2c_finance_vnd_fix bak ON bak.id = sbf.id
-- WHERE sbf.main_id = sb.id
--   AND sbf.is_deleted = FALSE AND sb.is_deleted = FALSE
--   AND sb.currency = 'VND' AND sbf.currency = 'CNY'
--   AND bak_main.exchange_rate IS NOT NULL AND bak_main.exchange_rate > 0;

COMMIT;


-- =============================================================================
-- Step 6: 修复后检查
-- =============================================================================

-- 6.1 汇率空值检查
SELECT 'so_outstock_detail' AS tbl, COUNT(*) AS null_rate_cnt
FROM so_outstock_detail
WHERE is_deleted = FALSE AND currency = 'VND' AND exchange_rate IS NULL
UNION ALL
SELECT 'so_return_instock_detail', COUNT(*)
FROM so_return_instock_detail
WHERE is_deleted = FALSE AND currency = 'VND' AND exchange_rate IS NULL
UNION ALL
SELECT 'so_b2c', COUNT(*)
FROM so_b2c
WHERE is_deleted = FALSE AND currency = 'VND' AND exchange_rate IS NULL;

-- 6.2 抽样对账：对比备份与修复后（WMS 销售出库）
SELECT
    bak.id,
    bak.exchange_rate AS bak_rate,
    cur.exchange_rate AS cur_rate,
    bak.all_amount_local_currency AS bak_all_amount_lc,
    cur.all_amount_local_currency AS cur_all_amount_lc
FROM bak_so_outstock_detail_vnd_fix bak
INNER JOIN so_outstock_detail cur ON cur.id = bak.id
WHERE bak.exchange_rate IS DISTINCT FROM cur.exchange_rate
LIMIT 20;

-- 6.3 抽样对账：对比备份与修复后（OMS B2C 明细成本）
SELECT
    bak.id,
    bak_main.exchange_rate AS bak_rate,
    sb.exchange_rate AS cur_rate,
    bak.tax_cost AS bak_tax_cost,
    sbd.tax_cost AS cur_tax_cost
FROM bak_so_b2c_detail_vnd_fix bak
INNER JOIN so_b2c_detail sbd ON sbd.id = bak.id
INNER JOIN so_b2c sb ON sb.id = sbd.main_id
INNER JOIN bak_so_b2c_vnd_fix bak_main ON bak_main.id = sb.id
WHERE bak_main.exchange_rate IS DISTINCT FROM sb.exchange_rate
LIMIT 20;


-- =============================================================================
-- 回滚脚本（仅在修复出错时使用，从 bak_* 表恢复）
-- =============================================================================
-- BEGIN;
--
-- UPDATE so_outstock_detail cur
-- SET
--     exchange_rate             = bak.exchange_rate,
--     all_amount_local_currency = bak.all_amount_local_currency,
--     update_time               = NOW()
-- FROM bak_so_outstock_detail_vnd_fix bak
-- WHERE cur.id = bak.id;
--
-- UPDATE so_return_instock_detail cur
-- SET
--     exchange_rate                    = bak.exchange_rate,
--     return_amount_local_currency     = bak.return_amount_local_currency,
--     tax_return_amount_local_currency = bak.tax_return_amount_local_currency,
--     update_time                      = NOW()
-- FROM bak_so_return_instock_detail_vnd_fix bak
-- WHERE cur.id = bak.id;
--
-- UPDATE so_b2c cur
-- SET
--     exchange_rate = bak.exchange_rate,
--     update_time   = NOW()
-- FROM bak_so_b2c_vnd_fix bak
-- WHERE cur.id = bak.id;
--
-- UPDATE so_b2c_detail cur
-- SET
--     exchange_rate            = bak.exchange_rate,
--     tax_cost                 = bak.tax_cost,
--     product_cost             = bak.product_cost,
--     first_mile_shipping_cost = bak.first_mile_shipping_cost,
--     clearance_customs_tax    = bak.clearance_customs_tax,
--     update_time              = NOW()
-- FROM bak_so_b2c_detail_vnd_fix bak
-- WHERE cur.id = bak.id;
--
-- UPDATE so_b2c_finance cur
-- SET
--     shipping_cost    = bak.shipping_cost,
--     item_cost        = bak.item_cost,
--     logistics_cost   = bak.logistics_cost,
--     platform_cost    = bak.platform_cost,
--     transfer_cost    = bak.transfer_cost,
--     accessories_cost = bak.accessories_cost,
--     vat_cost         = bak.vat_cost,
--     update_time      = NOW()
-- FROM bak_so_b2c_finance_vnd_fix bak
-- WHERE cur.id = bak.id;
--
-- COMMIT;
