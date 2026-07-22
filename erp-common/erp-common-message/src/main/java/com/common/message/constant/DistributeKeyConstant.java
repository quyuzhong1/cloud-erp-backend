package com.common.message.constant;

/**
 * @author zdy
 * @ClassName DistributeKeyConstant
 * @description: 分布式锁常量
 * @date 2026年04月01日
 * @version: 1.0
 */
public class DistributeKeyConstant {
    public static final String SO_B2C_ORDER_KEY = "SO_B2C_ORDER_KEY";
    public static final String SO_B2C_THIRD_DELIVERY_ORDER_KEY = "SO_B2C_THIRD_DELIVERY_ORDER_KEY";
    public static final String SO_B2C_DELIVERY_KEY = "SO_B2C_DELIVERY_KEY";
    public static final String SO_B2C_DELIVERY_INTERCEPT_KEY = "SO_B2C_DELIVERY_INTERCEPT_KEY";
    public static final String WMS_VIRTUAL_DETAIL_MSG_KEY = "WMS_VIRTUAL_DETAIL_MSG_KEY";
    public static final String PURCHASE_SUGGEST_MERGE = "PURCHASE_SUGGEST_MERGE";
    public static final String INVOICE_INFO_KEY = "INVOICE_INFO_KEY";
    // 工作流接口分布式锁KEY
    public static final String WORKFLOW_LOCK_KEY = "WORKFLOW_LOCK_KEY";
    /** 飞书审批回调 */
    public static final String WORKFLOW_FS_CALLBACK_KEY = "WORKFLOW_FS_CALLBACK";
    /** 第三方流程启动 */
    public static final String WORKFLOW_THIRD_START_KEY = "WORKFLOW_THIRD_START";
    /** 流程定义变更 */
    public static final String WORKFLOW_DEFINITION_CHANGE_KEY = "WORKFLOW_DEFINITION_CHANGE";
    /** 单据生命周期统一锁：提交、审批、撤销、回调状态更新使用同一业务id互斥 */
    public static final String BILL_BUSINESS_LOCK_KEY = "BILL_BUSINESS_LOCK";
    /** 采购订单QC合格数量更新 */
    public static final String SCM_PO_QC_QTY_KEY = "SCM_PO_QC_QTY";
    /** 采购订单到货状态更新 */
    public static final String SCM_PO_ARRIVAL_STATUS_KEY = "SCM_PO_ARRIVAL_STATUS";
    /** SRM采购订单确认 */
    public static final String SCM_SRM_ORDER_CONFIRM_KEY = "SCM_SRM_ORDER_CONFIRM";
    /** 采购订单生成送货单 */
    public static final String SCM_PO_GENERATE_DELIVERY_KEY = "SCM_PO_GENERATE_DELIVERY";
    /** 采购订单供应商确认 */
    public static final String SCM_PO_SUPPLIER_CONFIRM_KEY = "SCM_PO_SUPPLIER_CONFIRM";
    public static final String FIRST_MILE_WEIGHT_ALLOCATION_KEY = "FIRST_MILE_WEIGHT_ALLOCATION_KEY";
    /**
     * B2B销售订单key
     */
    public static final String SO_B2B_ORDER_KEY = "SO_B2B_ORDER_KEY";
    /**@DistributeLocker(businessType
     * tms 生成异步任务key
     */
    public static final String TMS_ASYNC_TASK_RECORD_KEY = "TMS_ASYNC_TASK_RECORD_KEY";
    /**
     * 头程报关单下推保存上锁 key，按来源发货单维度防止重复生成
     */
    public static final String TMS_DECLARE_BILL_FM_ADD_KEY = "TMS_DECLARE_BILL_FM_ADD_KEY";
    /**
     * B2B 报关单下推保存上锁 key，按来源发货通知单维度防止重复生成
     */
    public static final String TMS_DECLARE_BILL_B2B_ADD_KEY = "TMS_DECLARE_BILL_B2B_ADD_KEY";
    /**
     * 报关单批量保存上锁 key，覆盖下推合并/自动合并/拆分等共享同一保存链路的场景，按报关单或来源维度上锁
     */
    public static final String TMS_DECLARE_BILL_BATCH_SAVE_KEY = "TMS_DECLARE_BILL_BATCH_SAVE_KEY";

    /**
     * 报关单——按来源单 sourceId 互斥（新增 / 合并保存共用）
     * 用于防止同一来源单被并发生成多张报关单
     */
    public static final String TMS_DECLARE_BILL_SOURCE_KEY = "TMS_DECLARE_BILL_SOURCE_KEY";

    /**
     * 报关单——按报关单主键 id 互斥（编辑 / 拆分共用）
     * 用于防止同一张报关单被并发修改 / 拆分
     */
    public static final String TMS_DECLARE_BILL_ID_KEY = "TMS_DECLARE_BILL_ID_KEY";
    /**
     * TMS 异步任务执行锁（按 taskId 互斥，防 MQ 重投并发）
     */
    public static final String TMS_ASYNC_TASK_EXEC_KEY = "TMS_ASYNC_TASK_EXEC";
    /** 金蝶/WMS 同步 */
    public static final String KINGDEE_SYNC_KEY = "KINGDEE_SYNC";
    /** 出库单保存物流单 */
    public static final String SAVE_LOGISTICS_BILL_KEY = "SAVE_LOGISTICS_BILL_KEY";
    /** 退货入库生成物流单 */
    public static final String GENERATE_LOGISTICS_BILL_KEY = "GENERATE_LOGISTICS_BILL_KEY";
    /** 平台数据生成 B2C 出库单 */
    public static final String PLATFORM_GENERATE_SO_OUTSTOCK_KEY = "PLATFORM_GENERATE_SO_OUTSTOCK";
    /** 旺店通其他入库同步 */
    public static final String OTHER_INSTOCK_WDT_SYNC_KEY = "OTHER_INSTOCK_WDT_SYNC";
    /** 组包合并 */
    public static final String PACKAGE_MERGE_KEY = "PACKAGE_MERGE";
    /** FBA 装箱 */
    public static final String FBA_SHIPMENT_PACKING_KEY = "FBA_SHIPMENT_PACKING";
    /** 装箱/PDA 任务 */
    public static final String WMS_PACKING_TASK_KEY = "WMS_PACKING_TASK";
    /** 旺店通退货同步 */
    public static final String WDT_RETURN_SYNC_KEY = "WDT_RETURN_SYNC";
    /** 亚马逊报表任务 */
    public static final String AMZ_REPORT_TASK_KEY = "AMZ_REPORT_TASK";
    /** DMP 拉数任务 */
    public static final String DMP_PULL_TASK_KEY = "DMP_PULL_TASK";
    /** DMP 推送任务 */
    public static final String DMP_PUSH_TASK_KEY = "DMP_PUSH_TASK";
    /** DMP 售后单 */
    public static final String DMP_AFTER_SALE_KEY = "DMP_AFTER_SALE";
    /** 亚马逊店铺授权 */
    public static final String AMZ_AUTH_KEY = "AMZ_AUTH";
    /** 旺店通虚拟仓推送 */
    public static final String WDT_VW_PUSH_KEY = "WDT_VW_PUSH";
    /** DMP 清洗订单 */
    public static final String DMP_CLEAN_ORDER_KEY = "DMP_CLEAN_ORDER";
    /** B2C BOM 拆单 */
    public static final String SO_B2C_BOM_SPLIT_KEY = "SO_B2C_BOM_SPLIT";
    /** B2C 拆单保存 */
    public static final String SO_B2C_SPLIT_KEY = "SO_B2C_SPLIT";
    /** B2C 虚假发货重试 */
    public static final String SO_B2C_FALSE_DELIVERY_KEY = "SO_B2C_FALSE_DELIVERY";
    /** TMS 推送分摊 */
    public static final String TMS_PUSH_ALLOCATION_KEY = "TMS_PUSH_ALLOCATION";
    /** SRM 送货单 */
    public static final String SRM_DELIVERY_ORDER_KEY = "SRM_DELIVERY_ORDER";
    /** 系统用户改密 */
    public static final String SYS_USER_PWD_KEY = "SYS_USER_PWD";
    /** 系统用户资料 */
    public static final String SYS_USER_KEY = "SYS_USER";
    /** 系统用户权限关系 */
    public static final String SYS_USER_AUTH_KEY = "SYS_USER_AUTH";
    /** 系统模板默认值 */
    public static final String SYS_TEMPLATE_DEFAULT_KEY = "SYS_TEMPLATE_DEFAULT";
    /** 亚马逊订单下载 */
    public static final String AMZ_ORDER_DOWNLOAD_KEY = "AMZ_ORDER_DOWNLOAD";
    /** 销售出库单审核 */
    public static final String SO_OUTSTOCK_APPROVE_KEY = "SO_OUTSTOCK_APPROVE";
    /** 销售出库单生成 */
    public static final String SO_OUTSTOCK_GENERATE_KEY = "SO_OUTSTOCK_GENERATE";
    /** 虚拟库存覆盖 */
    public static final String WMS_VIRTUAL_OVERRIDE_KEY = "WMS_VIRTUAL_OVERRIDE";
    /** 其他入库审核状态更新 */
    public static final String OTHER_INSTOCK_APPROVE_KEY = "OTHER_INSTOCK_APPROVE";
    /** 其他出库审核状态更新 */
    public static final String OTHER_OUTSTOCK_APPROVE_KEY = "OTHER_OUTSTOCK_APPROVE";
    /** 直接调拨审核状态更新 */
    public static final String TRANSFER_INFO_APPROVE_KEY = "TRANSFER_INFO_APPROVE";
    /** 调拨申请审核状态更新 */
    public static final String TRANSFER_APPLICATION_APPROVE_KEY = "TRANSFER_APPLICATION_APPROVE";
    /** 采购收货新增 */
    public static final String WAREHOUSE_RECEIVE_ADD_KEY = "WAREHOUSE_RECEIVE_ADD";
    /** 销售退货入库保存 */
    public static final String SO_RETURN_INSTOCK_SAVE_KEY = "SO_RETURN_INSTOCK_SAVE";
    /** WMS 导入任务 */
    public static final String WMS_IMPORT_TASK_KEY = "WMS_IMPORT_TASK";
    /**
     * 物流商对账单变更锁（确认 / 校验切换 / 匹配 / 解绑 / 删除按对账单 mainId 互斥）
     */
    public static final String TMS_LOGISTICS_RECON_KEY = "TMS_LOGISTICS_RECON";

    /**
     * WMS 装箱单箱操作锁（暂存 / 完成 / 调整装箱共用）
     */
    public static final String WMS_PACKING_TASK_CARTON_KEY = "WMS_PACKING_TASK_CARTON_KEY";

    /**
     * WMS 装箱箱规操作锁
     */
    public static final String WMS_PACKING_TASK_CARTON_SPEC_KEY = "WMS_PACKING_TASK_CARTON_SPEC_KEY";

    /**
     * WMS 盘点下推：按库存维度（org+仓+库位+SKU+状态）联锁，串行预检与加 Redis 盘点锁
     */
    public static final String WMS_STOCKTAKING_INVENTORY_DIM_KEY = "WMS_STOCKTAKING_INVENTORY_DIM_KEY";
}
