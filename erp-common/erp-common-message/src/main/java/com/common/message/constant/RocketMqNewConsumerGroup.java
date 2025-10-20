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
    
    /**
     * 新中台旺店通退货单
     */
    public static final String DMP_WDT_ORDER_RETURN_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_order_return_to_wms_group";
    
    /**
     * 新中台旺店通销售出库单
     */
    public static final String DMP_WDT_SO_OUTSTOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_so_outstock_to_wms_group";
    /**
     * 新中台TeMu销售出库单
     */
    public static final String DMP_TEMU_SO_OUTSTOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_temu_so_outstock_to_wms_group";

    /**
     * 新中台平台商品
     */
    public static final String DMP_PLATFORM_PRODUCT_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_product_to_oms_group";
    /**
     * restcloud平台商品
     */
    public static final String RESTCLOUD_PLATFORM_PRODUCT_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_product_to_oms_group";

    /**
     * 新中台平台订单
     */
    public static final String DMP_PLATFORM_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_order_to_oms_group";
    /**
     * restcloud平台订单
     */
    public static final String RESTCLOUD_PLATFORM_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_order_to_oms_group";

    /**
     * restcloud平台退货订单
     */
    public static final String RESTCLOUD_PLATFORM_RETURN_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_return_order_to_oms_group";

    /**
     * 新中台Track123
     */
    public static final String DMP_TRACK123_TO_TMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_track123_to_tms_group";

    /**
     * 新中台平台仓库
     */
    public static final String DMP_PLATFORM_WAREHOUSE_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_warehouse_to_wms_group";

    /**
     * 新中台平台中转仓库
     */
    public static final String DMP_PLATFORM_TRANSFER_WAREHOUSE_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_transfer_warehouse_to_wms_group";


    /**
     * 新中台平台区域
     */
    public static final String DMP_PLATFORM_REGION_TO_SYS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_region_to_sys_group";

    /**
     * 新中台平台库存
     */
    public static final String DMP_PLATFORM_INVENTORY_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_inventory_to_wms_group";

    /**
     * 新中台平台入库
     */
    public static final String DMP_PLATFORM_INBOUND_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_inbound_to_wms_group";

    /**
     * 新中台平台出库
     */
    public static final String DMP_PLATFORM_OUTBOUND_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_outbound_to_wms_group";
    
    /**
     * 新中台旺店通仓库
     */
    public static final String DMP_WDT_WAREHOUSE_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_warehouse_to_dmp_group";
    
    /**
     * 新中台旺店通虚拟仓库
     */
    public static final String DMP_WDT_VIRTUALWAREHOUSE_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_virtualwarehouse_to_dmp_group";
    
    /**
     * 新中台旺店通店铺
     */
    public static final String DMP_WDT_SHOP_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_shop_to_dmp_group";
    
    /**
     * 新中台金蝶汇率
     */
    public static final String DMP_KINGDEE_EXCHANGERATE_TO_BI_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_exchangerate_to_bi_group";

    /**
     * 新中台金蝶产品
     */
    public static final String DMP_KINGDEE_PRODUCT_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_product_to_dmp_group";
    
    /**
     * 新中台金蝶店铺
     */
    public static final String DMP_KINGDEE_SHOP_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_shop_to_dmp_group";


    /**
     * 新中台金蝶收款银行
     */
    public static final String DMP_KINGDEE_BANK_TO_SYS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_kingdee_bank_to_sys_group";

    /**
     * 新中台旺店通预入库
     */
    public static final String DMP_WDT_PRE_STOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_wdt_pre_stock_to_wms_group";

    /**
     * 新中台平台库存
     */
    public static final String DMP_FBA_INVENTORY_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fba_inventory_to_wms_group";
    
    /**
     * 新中台推送产品上架时间
     */
    public static final String DMP_PRODUCT_LISTING_TO_PLM_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_product_listing_to_plm_group";


    /**
     * 新中台领星店铺
     */
    public static final String DMP_LX_SHOP_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_lx_shop_to_dmp_group";

    /**
     * 新中台领星FBA签收
     */
    public static final String DMP_LX_FBA_SHIPMENT_RECEIVE_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_lx_fba_shipment_receive_to_dmp_group";

    /**
     * 新中台平台FBA货件
     */
    public static final String DMP_FBA_SHIPMENT_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fba_shipment_to_wms_group";
    public static final String DMP_FULFILL_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fulfill_order_to_oms_group";

    /**
     * 新中台平台入库
     */
    public static final String DMP_AMZ_SO_OUT_STOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_amz_so_out_stock_to_wms_group";

    /**
     * 新中台平台退货入库
     */
    public static final String DMP_PLATFORM_RETURN_INSTOCK_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_return_instock_to_wms_group";

    /**
     * 新中台平台退货入库
     */
    public static final String DMP_PLATFORM_RETURN_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_return_order_to_oms_group";
    /**
     * 新中台平台退货入库
     */
    public static final String DMP_PLATFORM_REFUND_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_platform_refund_order_to_oms_group";
    /**
     * 新中台领星店铺
     */
    public static final String DMP_LX_SELLER_SHOP_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_lx_seller_shop_to_dmp_group";

    /**
     * 新中台第三方物流渠道
     */
    public static final String DMP_THIRD_LOGISTICS_TO_DMP_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_third_logistics_to_dmp_group";

    /**
     * 新中台飞书对接
     */
    public static final String DMP_FS_APPROVALS_TO_WORKFLOW_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fs_approvals_to_workflow_group";
    /**
     * 新中台飞书对接
     */
    public static final String DMP_FS_INSTANCES_TO_WORKFLOW_GROUP = "${spring.cloud.nacos.discovery.namespace}-dmp_fs_instances_to_workflow_group";

    /**
     * restcloudb2b订单
     */
    public static final String RESTCLOUD_PLATFORM_B2B_ORDER_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_b2b_order_to_oms_group";

    /**
     * 新中台平台收款单
     */
    public static final String RESTCLOUD_PLATFORM_RECEIPT_TO_OMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_receipt_to_oms_group";

    /**
     * restcloud平台仓库
     */
    public static final String RESTCLOUD_PLATFORM_WAREHOUSE_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_warehouse_to_wms_group";

    /**
     * 新中台平台入库
     */
    public static final String RESTCLOUD_PLATFORM_INBOUND_TO_WMS_GROUP = "${spring.cloud.nacos.discovery.namespace}-restcloud_platform_inbound_to_wms_group";


}
