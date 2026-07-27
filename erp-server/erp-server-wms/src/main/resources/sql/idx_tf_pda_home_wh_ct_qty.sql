-- =============================================================================
-- PDA 首页 getInventoryByWarehouseId / sumTodayFlowForPdaHome 性能优化
-- 库：prod-erp-wms（及对应环境 WMS 库）
-- 说明：为当日流水按仓汇总提供 INCLUDE(qty) 覆盖索引，减少回表与 lossy bitmap
-- 执行：请 DBA 在业务低峰使用 CONCURRENTLY 创建；勿包在事务块中
-- =============================================================================

-- 1. 新建覆盖索引（推荐先建、观察后再考虑删旧索引）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tf_pda_home_wh_ct_qty
    ON transaction_flow (warehouse_id, create_time)
    INCLUDE (qty)
    WHERE is_deleted = false
      AND is_unapproved = false
      AND dict_inventory_status = 'usable';

COMMENT ON INDEX idx_tf_pda_home_wh_ct_qty IS
    'PDA首页当日出入库汇总：warehouse_id+create_time 覆盖 qty，支持 Index Only Scan';

-- 2. （可选）可用库存按仓汇总，避免 inventory 并行顺序扫
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_inv_pda_home_wh_qty
    ON inventory (warehouse_id)
    INCLUDE (qty)
    WHERE is_deleted = false
      AND dict_inventory_status = 'usable';

COMMENT ON INDEX idx_inv_pda_home_wh_qty IS
    'PDA首页可用库存按仓汇总：warehouse_id 覆盖 qty';

-- 3. （可选，稳定后）删除旧索引，避免重复维护
-- 确认查询计划已使用 idx_tf_pda_home_wh_ct_qty 后再执行：
-- DROP INDEX CONCURRENTLY IF EXISTS idx_tf_pda_home_wh_ct;

-- 4. 验证示例（替换仓库 ID）
-- EXPLAIN (ANALYZE, BUFFERS)
-- SELECT
--     SUM(qty) FILTER (WHERE qty > 0) AS today_stock_in_qty,
--     SUM(qty) FILTER (WHERE qty < 0) AS today_delivery_qty
-- FROM transaction_flow
-- WHERE is_deleted = FALSE
--   AND is_unapproved = FALSE
--   AND dict_inventory_status = 'usable'
--   AND warehouse_id = '1676949540114010116'
--   AND create_time >= CURRENT_DATE
--   AND create_time < CURRENT_DATE + INTERVAL '1 day';
