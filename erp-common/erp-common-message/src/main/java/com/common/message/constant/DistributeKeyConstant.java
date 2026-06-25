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
     * TMS 异步任务执行锁（按 taskId 互斥，防 MQ 重投并发）
     */
    public static final String TMS_ASYNC_TASK_EXEC_KEY = "TMS_ASYNC_TASK_EXEC";
    /** 金蝶/WMS 同步 */
    public static final String KINGDEE_SYNC_KEY = "KINGDEE_SYNC";
    /** 出库单保存物流单 */
    public static final String SAVE_LOGISTICS_BILL_KEY = "saveLogisticsBill";
    /** 退货入库生成物流单 */
    public static final String GENERATE_LOGISTICS_BILL_KEY = "generateLogisticsBill";
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
    /** 亚马逊订单下载 */
    public static final String AMZ_ORDER_DOWNLOAD_KEY = "AMZ_ORDER_DOWNLOAD";
    /** 销售出库单审核 */
    public static final String SO_OUTSTOCK_APPROVE_KEY = "SO_OUTSTOCK_APPROVE";
}
