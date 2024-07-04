package com.common.message.constant;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 11:56
 */
public class RocketMqNewConsumerGroup {
	/**
     * 新中台金蝶调拨单
     */
    public static final String DMP_KINGDEE_DIRECT_TRANSFER_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_direct_transfer_to_wms_group";
    
    /**
     * 新中台金蝶退货单
     */
    public static final String DMP_KINGDEE_ORDER_RETURN_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_order_return_to_wms_group";
    
    /**
     * 新中台金蝶销售出库单
     */
    public static final String DMP_KINGDEE_SO_OUTSTOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_so_outstock_to_wms_group";
}
