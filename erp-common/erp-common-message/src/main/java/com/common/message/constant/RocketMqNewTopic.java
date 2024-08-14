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
     * 新中台Track123
     */
    public static final String DMP_TRACK123_TO_TMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_track123_to_tms_topic";

    /**
     * 新中台平台仓库
     */
    public static final String DMP_PLATFORM_WAREHOUSE_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_warehouse_to_wms_topic";

    /**
     * 新中台平台中转仓库
     */
    public static final String DMP_PLATFORM_TRANSFER_WAREHOUSE_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_transfer_warehouse_to_wms_topic";

    /**
     * 新中台平台区域
     */
    public static final String DMP_PLATFORM_REGION_TO_SYS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_region_to_sys_topic";

    /**
     * 新中台平台库存
     */
    public static final String DMP_PLATFORM_INVENTORY_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_inventory_to_wms_topic";

    /**
     * 新中台平台入库
     */
    public static final String DMP_PLATFORM_INBOUND_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_inbound_to_wms_topic";


    /**
     * 新中台平台入库
     */
    public static final String DMP_PLATFORM_OUTBOUND_TO_WMS_TOPIC = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_outbound_to_wms_topic";
}
