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
    
    /**
     * 新中台金蝶销售出库单
     */
    public static final String DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_so_outstock_to_wms_topic";
    
    /**
     * 新中台旺店通退货单
     */
    public static final String DMP_WDT_ORDER_RETURN_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_order_return_to_wms_topic";
    
    /**
     * 新中台旺店通销售出库单
     */
    public static final String DMP_WDT_SO_OUTSTOCK_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_so_outstock_to_wms_topic";
    
    /**
     * 新中台平台商品
     */
    public static final String DMP_PLATFORM_PRODUCT_TO_OMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_product_to_oms_topic";
    
    /**
     * 新中台平台订单
     */
    public static final String DMP_PLATFORM_ORDER_TO_OMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_order_to_oms_topic";
    

    /**
     * 新中台旺店通预入库
     */
    public static final String DMP_WDT_PRE_STOCK_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_pre_stock_to_wms_topic";
}
