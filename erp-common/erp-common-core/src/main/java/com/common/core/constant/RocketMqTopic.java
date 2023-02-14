package com.common.core.constant;

/**
 * rocket mq  topic
 *
 * @Author Cloud
 * @Date 2023/2/3 16:55
 **/
public class RocketMqTopic {

    /**
     * dmp topic
     */
    public static final String DMP_TOPIC = "dmp_erp_pull_topic";

    /**
     * dmp consumer topic
     */
    public static final String DMP_CONSUMER_TOPIC = "${spring.profiles.active}-dmp_erp_pull_topic";
}
