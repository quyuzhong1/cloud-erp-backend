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
}
