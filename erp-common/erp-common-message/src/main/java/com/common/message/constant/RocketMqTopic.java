package com.common.message.constant;

/**
 * rocket mq  topic
 *
 * @Author Cloud
 * @Date 2023/2/3 16:55
 **/
public class RocketMqTopic {


    /**
     * dmp 拉取第三方ERP订单topic
     */
    public static final String DMP_ERP_ORDER_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_erp_pull_topic";

    /**
     * 推送数据到金蝶
     */
    public static final String SYNC_KINGDEE_ERP_TOPIC = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_erp_topic";

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
     * 发送预警消息到消息服务主题
     */
    public static final String WARN_MSG_TOPIC = "${spring.cloud.nacos.discovery.namespace}-warn_msg_topic";

    /**
     * dmp 拉取第三方ERP订单topic
     */
    public static final String DMP_ERP_DATA_CLEAN_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_erp_data_clean_topic";

}
