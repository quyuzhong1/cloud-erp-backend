package com.common.message.constant;

/**
 * rocket mq  topic
 *
 * @Author Cloud
 * @Date 2023/2/3 16:55
 **/
public class RocketMqNewTopic {
	/**
     * 新中台金蝶调拨单
     */
    public static final String DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_direct_transfer_to_wms_topic";
    
    /**
     * 新中台金蝶退货单
     */
    public static final String DMP_KINGDEE_ORDER_RETURN_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_order_return_to_wms_topic";
}
