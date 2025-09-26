package com.common.message.constant;

/**
 * rocket mq  topic
 *
 * @Author Cloud
 * @Date 2023/2/3 16:55
 **/
public class RocketMqTopic {

    /**
     * 推送系统日志数据
     */
    public static final String SYNC_ERP_LOG_TO_SYS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_erp_log_to_sys_topic";

    /**
     * 接收ddl 的变化后的数据库数据
     */
    public static final String RECEIVE_DDL_TO_MQ_SYS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-receive_ddl_to_mq_sys_topic";
    /**
     * 推送三方通知
     */
    public static final String SEND_THIRD_NOTICE_SYS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-send_third_notice_sys_topic";

    /**
     * dmp 拉取第三方ERP订单topic
     */
    public static final String DMP_ERP_ORDER_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_erp_pull_topic";

    /**
     * 同步内部订单到dmp topic
     */
    public static final String SYNC_SO_INFO_ORDER_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_so_info_order_to_dmp_topic";
    public static final String SYNC_SO_OUTSTOCK_ORDER_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_so_outstock_order_to_dmp_topic";
    public static final String SYNC_RETURN_ORDER_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_return_order_to_dmp_topic";
    public static final String SYNC_SO_B2C_ORDER_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_so_b2c_order_to_dmp_topic";
    public static final String SYNC_SO_B2C_DELIVERY_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_so_b2c_delivery_to_dmp_topic";
    /**
     * 推送数据到金蝶
     */
    public static final String SYNC_KINGDEE_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_erp_topic";
    public static final String SYNC_AMAZON_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_amazon_erp_topic";
    /**
     * 推送数据到金蝶
     */
    public static final String SYNC_WANGDIAN_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_wangdian_erp_topic";
    /**
     * 推送数据到金蝶
     */
    public static final String SYNC_LINGXING_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_lingxing_erp_topic";
    /**
     * 推送Plm产品信息到dmp
     */
    public static final String SYNC_PLM_PRODUCT_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_plm_product_topic";

    /**
     * 发送任务消息到消息服务主题
     */
    public static final String NOTICE_MSG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-notice_msg_topic";

    /**
     * dmp数据更新
     */
    public static final String DMP_ERP_ORDER_UPDATE_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_erp_order_update_topic";

    /**
     * 推送Plm产品信息到wms
     */
    public static final String SYNC_PLM_TO_WMS_PRODUCT_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_plm_to_wms_product_topic";

    /**
     * 推送Scm采购单信息到Wms
     */
    public static final String SYNC_SCM_TO_WMS_PURCHASE_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_scm_to_wms_purchase_topic";

    /**
     * 推送Scm采购单信息到SRM
     */
//    public static final String SYNC_SCM_TO_SRM_PURCHASE_ORDER_DETAIL_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_scm_to_srm_purchase_order_detail_topic";
    /**
     * 同步金蝶销售信息到OMS
     */
    public static final String SYNC_KINGDEE_TO_OMS_SALES_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_to_oms_sales_topic";

    /**
     * 同步金蝶销售出库单到WMS
     */
    public static final String SYNC_KINGDEE_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_to_wms_topic";

    /**
     * 发送预警消息到消息服务主题
     */
    public static final String WARN_MSG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-warn_msg_topic";

    /**
     * dmp 拉取第三方ERP订单topic
     */
    public static final String DMP_ERP_DATA_CLEAN_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_erp_data_clean_topic";

    /**
     * WMS推送数据到DMP
     */
    public static final String SYNC_WMS_TO_DMP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_wms_to_dmp_topic";

    /**
     * DMP推送数据到马帮
     */
    public static final String SYNC_DMP_TO_MABANG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_dmp_to_mabang_topic";

    /**
     * DMP同步任务
     */
    public static final String DMP_SYNC_TASK_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_sync_task_topic";

    /**
     * dmp下载第三方数据
     */
    public static final String PLATFORM_PULL_DATA_TOPIC = "${spring.cloud.nacos.discovery.namespace}-platform_pull_data_topic";
    /**
     * dmp推送第三方平台数据
     */
    public static final String PLATFORM_PUSH_DATA_TOPIC = "${spring.cloud.nacos.discovery.namespace}-platform_push_data_topic";


    /**
     * 推送系统日志数据
     */
    public static final String AMZ_REPORT_TASK_TOPIC = "${spring.cloud.nacos.discovery.namespace}-amz_report_task_topic";

    /**
     * 异步获取平台打印面单标签
     */
    public static final String ASYNC_GET_PLATFORM_LABEL_TOPIC = "${spring.cloud.nacos.discovery.namespace}-async_get_platform_label_topic";

    /**
     * 异步组包发货
     */
    public static final String ASYNC_MERGE_PACKAGE_DELIVERY_TOPIC = "${spring.cloud.nacos.discovery.namespace}-async_merge_package_delivery_topic";

    /**
     * 推送数据到数帝云
     */
    public static final String SYNC_SUDUYUN_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_sudiyun_erp_topic";

    /**
     * 同步虚拟仓库存明细
     */
    public static final String WMS_VIRTUAL_DETAIL_MSG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-wms_virtual_detail_msg_topic";
    /**
     * 同步track123 物流轨迹更新记录
     */
    public static final String TMS_123_LOGISTICS_TRACK = "${spring.cloud.nacos.discovery.namespace}-tms_track123_logistics_track_msg_topic";

    /**
     *  DMP 微信订阅消息 消费者 Subscribe
     */
    public static final String DMP_WECHAT_SUBSCRIBE_MSG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_wechat_subscribe_msg_topic";

    /**
     *  workflow 同步飞书流程实例
     */
    public static final String WORKFLOW_SYNC_FS_INSTANCE_TOPIC = "${spring.cloud.nacos.discovery.namespace}-workflow_sync_fs_instance_topic";

    /**
     *   workflow 同步飞书审批定义
     */
    public static final String DMP_PLATFORM_APPROVALS_TO_WORKFLOW_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_approvals_to_workflow_topic";

    /**
     * OMS 任务编排
     */
    public static final String OMS_WORKFLOW_TASK_RECORD_TOPIC = "${spring.cloud.nacos.discovery.namespace}-oms_workflow_task_record_topic";
}
