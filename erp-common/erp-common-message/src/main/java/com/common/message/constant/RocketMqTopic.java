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

}
