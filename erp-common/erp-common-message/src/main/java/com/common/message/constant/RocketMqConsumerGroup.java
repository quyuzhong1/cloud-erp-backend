package com.common.message.constant;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 11:56
 */
public class RocketMqConsumerGroup {

    /**
     * 金蝶物料（产品信息）
     */
    public static final String SYNC_KINGDEE_PRODUCT_DETAIL = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_product_detail";
    /**
     * 金蝶物料清单（BOM管理）
     */
    public static final String SYNC_KINGDEE_BOM_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_bom_info";
    /**
     * 金蝶辅助资料（产品分类、）
     */
    public static final String SYNC_KINGDEE_ASSISTANT_DATA = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_assistant_data";
    /**
     * 金蝶员工（用户管理）
     */
    public static final String SYNC_KINGDEE_SYS_USER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_user_info";
    /**
     * 金蝶部门（部门管理）
     */
    public static final String SYNC_KINGDEE_SYS_DEPARTMENT = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_department";


    /**
     * 金蝶岗位
     */
    public static final String SYNC_KINGDEE_SYS_POST = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_post";


    /**
     * 金蝶员工任岗
     */
    public static final String SYNC_KINGDEE_SYS_USER_POST = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_sys_user_post";

    /**
     * 金蝶员工任岗
     */
    public static final String SYNC_KINGDEE_OPERATOR = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_operator";

    /**
     * 金蝶采购订单（采购订单）
     */
    public static final String SYNC_KINGDEE_PURCHASE_ORDER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_order";
    /**
     * 金蝶采购订单新变更单（采购变更）
     */
    public static final String SYNC_KINGDEE_PURCHASE_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_change";

    /**
     * 金蝶采购价目表（采购价目表）
     */
    public static final String SYNC_KINGDEE_PURCHASE_PRICE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_price";

    /**
     * 金蝶采购调价表（采购调价表）
     */
    public static final String SYNC_KINGDEE_PURCHASE_PRICE_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_price_change";

    /**
     * 金蝶退货单
     */
    public static final String SYNC_KINGDEE_PURCHASE_RETURN_ORDER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_purchase_return_order";

    /**
     * 金蝶仓库
     */
    public static final String SYNC_KINGDEE_WAREHOUSE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_warehouse";

    /**
     * 金蝶供应商
     */
    public static final String SYNC_KINGDEE_SUPPLIER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_supplier";

    /**
     * 金蝶入库单
     */
    public static final String SYNC_KINGDEE_PURCHASE_STOCK_IN = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_stock_in";

    /**
     * 金蝶直接调拨单
     */
    public static final String SYNC_KINGDEE_TRANSFER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_transfer_info";

    /**
     * 客户列表
     */
    public static final String SYNC_KINGDEE_CUSTOMER_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_info";

    /**
     * 客户分组
     */
    public static final String SYNC_KINGDEE_CUSTOMER_GROUP = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_group";

    /**
     * 客户分组
     */
    public static final String SYNC_KINGDEE_CUSTOMER_CONTACT = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_customer_contact";

    /**
     * 其他出库
     */
    public static final String SYNC_KINGDEE_OTHER_OUTSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_other_outstock";

    /**
     * 其他入库
     */
    public static final String SYNC_KINGDEE_OTHER_INSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_other_instock";

    /**
     * 加工单
     */
    public static final String SYNC_KINGDEE_MACHINE_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_machine_info";

    /**
     * 销售订单(金蝶推送)
     */
    public static final String SYNC_KINGDEE_SO_INFO = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_info";

    /**
     * 销售订单
     */
    public static final String SYNC_KINGDEE_SO_INFO1 = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_info1";

    /**
     * 销售订单变更
     */
    public static final String SYNC_KINGDEE_SO_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_change";

    /**
     * 销售出库(金蝶推送)
     */
    public static final String SYNC_KINGDEE_SO_OUTSTOCK = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_outstock";


    /**
     * 销售出库
     */
    public static final String SYNC_KINGDEE_SO_OUTSTOCK1 = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_outstock1";

    /**
     * 销售退货单
     */
    public static final String SYNC_KINGDEE_SO_RETURN = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_return";
    /**
     * 订单 oms推送到dmp
     */
    public static final String SYNC_OMS_TO_DMP_ORDER = "${spring.cloud.nacos.discovery.namespace}-approved_order_to_dmp_consumer";

    /**
     * 订单 oms推送到dmp
     */
    public static final String SYNC_OMS_RETURN_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_oms_return_to_dmp";

    /**
     * 委外订单
     */
    public static final String SYNC_KINGDEE_SUBCONTRACT_ORDER = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_subcontract_order";

    /**
     * 委外变更订单
     */
    public static final String SYNC_KINGDEE_SUBCONTRACT_CHANGE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_subcontract_change";

    /**
     * 盘盈单 StocktakingProfit
     */
    public static final String SYNC_KINGDEE_STOCKTAKING_PROFIT = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_stocktaking_profit";

    /**
     * 盘亏单
     */
    public static final String SYNC_KINGDEE_STOCKTAKING_LOSS = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_stocktaking_loss";


    /**
     * 委外发料单
     */
    public static final String SYNC_KINGDEE_SUBCONTRACT_ISSUE = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_subcontract_issue";

    //-----------------------------------------------------------------dmp数据更新------------------------------------------------------

    /**
     * 店铺变更负责人
     */
    public static final String SHOP_INFO_CHANGE_CHARGE = "${spring.cloud.nacos.discovery.namespace}-shop_info_change_charge";

    /**
     * 店铺变更部门
     */
    public static final String SHOP_INFO_CHANGE_DEPT = "${spring.cloud.nacos.discovery.namespace}-shop_info_change_dept";

    /**
     * 汇率变更
     */
    public static final String CHANGE_CURRENCY = "${spring.cloud.nacos.discovery.namespace}-change_currency";

    /**
     * 产品上市时间
     */
    public static final String PRODUCT_LISTING_UPDATE = "${spring.cloud.nacos.discovery.namespace}-product_listing_update";


    //-----------------------------------------------------------------wms数据更新------------------------------------------------------
    /**
     * 金蝶B2C 销售出库单同步
     */
    public static final String SYNC_KINGDEE_SO_OUTSTOCK_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_so_outatock_to_wms";

    /**
     * 直接调拨单金蝶同步至ERP
     */
    public static final String SYNC_KINGDEE_TRANSFER_INFO_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_transfer_info_to_wms";

    /**
     * ERP直接调拨单->DMP
     */
    public static final String SYNC_ERP_TRANSFER_INFO_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_erp_transfer_info_to_dmp";

    /**
     * DMP出入库->马帮
     */
    public static final String SYNC_DMP_TRANSFER_INFO_TO_MABANG = "${spring.cloud.nacos.discovery.namespace}-sync_dmp_transfer_info_to_mabang";

    /**
     * 马帮FBA发货单同步到WMS
     */
    public static final String SYNC_MABANG_FBA_DELIVERY_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_mabang_fba_delivery_to_wms";

    /**
     * 金蝶退货单同步
     */
    public static final String SYNC_KINGDEE_RETURN_ORDER_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_return_order_to_wms";

    /**
     * ERP加工单单->DMP
     */
    public static final String SYNC_ERP_MACHINE_INFO_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_ermachine_info_to_dmp";

    /**
     * ERP采购收货单->DMP
     */
    public static final String SYNC_ERP_PO_RECEIVE = "${spring.cloud.nacos.discovery.namespace}-sync_erp_po_receive";

    /**
     * 金蝶汇率列表同步至ERP
     */
    public static final String SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_kingdee_exchange_rate_to_wms";

    /**
     * 亚马逊订单推送服务tag
     */
    public static final String SYNC_AMAZON_ORDER_FROM_OMS = "${spring.cloud.nacos.discovery.namespace}-sync_amazon_order_from_oms";

    /**
     * 添加系统操作日志
     */
    public static final String SYNC_ERP_LOG_TO_SYS = "${spring.cloud.nacos.discovery.namespace}-sync_erp_log_to_sys";

    /**
     * ERP B2c订单->DMP
     */
    public static final String SYNC_ERP_SO_B2C_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_erp_so_b2c_to_dmp";

    /**
     * ERP B2c发货单->DMP
     */
    public static final String SYNC_ERP_SO_B2C_DELIVERY_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_erp_so_b2c_delivery_to_dmp";

    /**
     * ERP 销售出库单->DMP发货详情
     */
    public static final String SYNC_SO_OUTSTOCK_TO_DMP = "${spring.cloud.nacos.discovery.namespace}-sync_so_outstock_to_dmp";

    /**
     * 亚马逊创建报告
     */
    public static final String SYNC_AMZ_REPORT_CREATE = "${spring.cloud.nacos.discovery.namespace}-sync_amz_report_create_consumer";
    /**
     * 亚马逊查询报告
     */
    public static final String SYNC_AMZ_REPORT_QUERY = "${spring.cloud.nacos.discovery.namespace}-sync_amz_report_query_consumer";

    /**
     * 亚马逊下载报告
     */
    public static final String SYNC_AMZ_REPORT_DOWNLOAD = "${spring.cloud.nacos.discovery.namespace}-sync_amz_report_download_consumer";

    /**
     * 亚马逊解析报告
     */
    public static final String SYNC_AMZ_REPORT_PARSE = "${spring.cloud.nacos.discovery.namespace}-sync_amz_report_parse_consumer";

    /**
     * 亚马逊直接查询报告
     */
    public static final String SYNC_AMZ_REPORT_DIRECT_QUERY = "${spring.cloud.nacos.discovery.namespace}-sync_amz_report_direct_query_consumer";

    /**
     * 异步获取平台打印面单标签
     */
    public static final String ASYNC_GET_PLATFORM_LABEL_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-async_get_platform_label_consumer";
    /**
     * 异步更新物流更新时间
     */
    public static final String ASYNC_GET_LOGISTICS_TRACK_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-async_get_logistics_track_consumer";

    /**
     * 异步组包发货
     */
    public static final String ASYNC_MERGE_PACKAGE_DELIVERY_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-async_merge_package_delivery_consumer";

    /**
     * 同步虚拟仓库存明细
     */
    public static final String WMS_VIRTUAL_DETAIL_MSG_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-wms_virtual_detail_msg_consumer";


    /**
     *
     */
    public static final String DMP_WECHAT_SUBSCRIBE_MSG_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-dmp_wechat_subscribe_msg_consumer";

    /**
     *
     */
    public static final String WORKFLOW_SYNC_FS_INSTANCE_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-workflow_sync_fs_instance_consumer";


    /**
     *
     */
    public static final String SYS_SEND_THIRD_NOTICE_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-sys_send_third_notice_consumer";

    /**
     *
     */
    public static final String SYS_RECEIVE_DDL_TO_MQ_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-sys_receive_ddl_to_mq_consumer";

    /**
     *
     */
    public static final String WORKFLOW_FS_APPROVALS_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-workflow_fs_approvals_consumer";

    /**
     *
     */
    public static final String WORKFLOW_FS_INSTANCES_CONSUMER = "${spring.cloud.nacos.discovery.namespace}-workflow_fs_instances_consumer";

    /**
     * 添加系统操作日志
     */
    public static final String OMS_WORKFLOW_TASK_RECORD = "${spring.cloud.nacos.discovery.namespace}-oms_workflow_task_record";
    //---------------------------------------------------wangdian---------------------------------------------------------------------------------------------
    /**
     * 旺店通推送货品资料
     */
    public static final String SYNC_WDT_PRODUCT_DETAIL = "${spring.cloud.nacos.discovery.namespace}-sync_wdt_product_detail";

    public static final String SYNC_WDT_OUT_STOCK_TO_WMS = "${spring.cloud.nacos.discovery.namespace}-sync_wdt_out_stock_to_wms";

    public static final String SYNC_WDT_OTHER_IN_STOCK = "${spring.cloud.nacos.discovery.namespace}-sync_wdt_other_in_stock";

    public static final String SYNC_WDT_OTHER_OUT_STOCK = "${spring.cloud.nacos.discovery.namespace}-sync_wdt_other_out_stock";
    public static final String SYNC_WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL = "${spring.cloud.nacos.discovery.namespace}-sync_wdt_virtual_allocation_handle_detail";


}
