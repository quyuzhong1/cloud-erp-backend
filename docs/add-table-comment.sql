-- =============================================================
-- 表注释补充脚本（PostgreSQL）
-- 按业务库（模块）分组，请在对应库分别执行
-- =============================================================


-- =============================================================
-- 【erp-wms 仓储库】
-- =============================================================

-- AWD 出库 / 库存
COMMENT ON TABLE "public"."awd_outstock" IS 'AWD出库货件主表';
COMMENT ON TABLE "public"."awd_outstock_detail" IS 'AWD出库货件明细表';
COMMENT ON TABLE "public"."awd_inventory" IS 'AWD库存表';

-- 质检（QC）
COMMENT ON TABLE "public"."qc_defect" IS '质检缺陷表';
COMMENT ON TABLE "public"."qc_sampling_plan_ref" IS '质检抽样方案关联表';
COMMENT ON TABLE "public"."qc_standard_ref" IS '质检标准关联表';
COMMENT ON TABLE "public"."qc_standard_image_ref" IS '质检标准图片关联表';

-- 第三方仓库操作说明配置
COMMENT ON TABLE "public"."cfg_third_warehouse_operation_description" IS '第三方仓库操作说明配置表';
COMMENT ON TABLE "public"."cfg_third_warehouse_operation_description_value" IS '第三方仓库操作说明值配置表';

-- 报表 / FBA
COMMENT ON TABLE "public"."fba_transit_calculate_detail_report" IS 'FBA在途测算明细报表';
COMMENT ON TABLE "public"."report_order_data" IS '报表订单数据表';
COMMENT ON TABLE "public"."fba_shipment_receive_check" IS 'FBA货件收货核对表';

-- 销售出库辅助表（归档 / 映射 / 临时，需确认）
COMMENT ON TABLE "public"."so_outstock_archive" IS '销售出库归档表';
COMMENT ON TABLE "public"."so_outstock_mapping" IS '销售出库映射表';
COMMENT ON TABLE "public"."temp_recovry_record" IS '临时恢复记录表';

-- 附件（wms 库）
COMMENT ON TABLE "public"."attachment" IS '附件表';
COMMENT ON TABLE "public"."attachment_20260415" IS '附件表备份(20260415)';


-- =============================================================
-- 【其他业务库】attachment 为各业务库通用表
-- 如下库同样存在 attachment 表，按需在对应库执行
-- erp-scm / erp-tms / erp-plm / erp-dmp / erp-srm / erp-fms / erp-oms
-- =============================================================

-- COMMENT ON TABLE "public"."attachment" IS '附件表';
