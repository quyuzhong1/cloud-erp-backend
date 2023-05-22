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
    public static final String DMP_ERP_ORDER_TOPIC = "${spring.profiles.active}-dmp_erp_pull_topic";

    /**
     * 推送数据到金蝶
     */
    public static final String SYNC_KINGDEE_ERP_TOPIC = "${spring.profiles.active}-sync_kingdee_erp_topic";

    /**
     * 推送Plm产品信息到dmp
     */
    public static final String SYNC_PLM_PRODUCT_TOPIC = "${spring.profiles.active}-sync_plm_product_topic";

    /**
     * 发送消息到消息服务主题
     */
    public static final String NOTICE_MSG_TOPIC = "${spring.profiles.active}-notice_msg_topic";

    /**
     * dmp数据更新
     */
    public static final String DMP_ERP_ORDER_UPDATE_TOPIC = "${spring.profiles.active}-dmp_erp_order_update_topic";

    /**
     * 推送Plm产品信息到wms
     */
    public static final String SYNC_PLM_TO_WMS_PRODUCT_TOPIC = "${spring.profiles.active}-sync_plm_to_wms_product_topic";

}
