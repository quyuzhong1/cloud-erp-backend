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
}
