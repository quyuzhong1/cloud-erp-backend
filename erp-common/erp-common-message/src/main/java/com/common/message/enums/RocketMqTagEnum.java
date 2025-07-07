package com.common.message.enums;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp topic 使用 RocketMqTopic.DMP_ERP_ORDER_TOPIC
 * tag 区分不同类型数据方便订阅
 * tag 统一归集配置常量 避免不同业务发生冲突
 *
 * @Author Cloud
 * @Date 2023/2/6 10:32
 **/
@Slf4j
public enum RocketMqTagEnum {

    //---------------------------------中台数据抓取从1开始------------------------------------------------------------------------------------------

    /**
     * 管易销售订单tag
     */
    GYY_SALE_ORDER_TAG(1,RocketMqTagEnum.SALES_ORDER,"gyy_sales_order_tag"),
    /**
     * 管易销售历史订单tag
     */
    GYY_SALE_HISTORY_ORDER_TAG(2,RocketMqTagEnum.SALES_ORDER,"gyy_sales_history_order_tag"),
    /**
     * 管易发货订单tag
     */
    GYY_DELIVERY_ORDER_TAG(3,RocketMqTagEnum.DELIVERY_ORDER,"gyy_delivery_order_tag"),
    /**
     * 管易发货历史订单tag
     */
    GYY_DELIVERY_HISTORY_ORDER_TAG(4,RocketMqTagEnum.DELIVERY_ORDER,"gyy_delivery_history_order_tag"),
    /**
     * 管易退款订单tag
     */
    GYY_REFUND_ORDER_TAG(5,RocketMqTagEnum.REFUND_ORDER,"gyy_refund_order_tag"),
    /**
     * 管易退货订单tag
     */
    GYY_RETURN_ORDER_TAG(6,RocketMqTagEnum.RETURN_ORDER,"gyy_return_order_tag"),
    /**
     * 管易店铺信息tag
     */
    GYY_SHOP_INFO_TAG(7,RocketMqTagEnum.SHOP_INFO,"gyy_shop_info_tag"),
    /**
     * 管易SKU信息tag
     */
    GYY_SKU_INFO_TAG(8,RocketMqTagEnum.SKU_INFO,"gyy_sku_info_tag"),

    /**
     * 金蝶销售订单tag
     */
    KINGDEE_SALE_ORDER_TAG(9,RocketMqTagEnum.SALES_ORDER,"kingdee_sales_order_tag"),

    /**
     * 金蝶发货订单tag
     */
    KINGDEE_DELIVERY_ORDER_TAG(10,RocketMqTagEnum.DELIVERY_ORDER,"kingdee_delivery_order_tag"),
    /**
     * 金蝶退款订单tag
     */
    KINGDEE_REFUND_ORDER_TAG(11,RocketMqTagEnum.REFUND_ORDER,"kingdee_refund_order_tag"),
    /**
     * 金蝶退货订单tag
     */
    KINGDEE_RETURN_ORDER_TAG(12,RocketMqTagEnum.RETURN_ORDER,"kingdee_return_order_tag"),
    /**
     * 金蝶订单tag
     */
    KINGDEE_SHOP_INFO_TAG(13,RocketMqTagEnum.SHOP_INFO,"kingdee_shop_info_tag"),
    /**
     * 金蝶退货订单tag
     */
    KINGDEE_ECC_SHOP_INFO_TAG(21,RocketMqTagEnum.SHOP_INFO,"kingdee_ecc_shop_info_tag"),
    /**
     * 金蝶sku信息tag
     */
    KINGDEE_SKU_INFO_TAG(14,RocketMqTagEnum.SKU_INFO,"kingdee_sku_info_tag"),

    /**
     * 金蝶直接调拨订单tag
     */

    KINGDEE_TRANSFER_DIRECT_TAG(15,RocketMqTagEnum.TRANSFER,"kingdee_transfer_direct_tag"),
    /**
     * 马帮销售订单tag
     */
    MABANG_SALE_ORDER_TAG(15,RocketMqTagEnum.SALES_ORDER,"mabang_sales_order_tag"),

    /**
     * 马帮发货订单tag
     */
    MABANG_DELIVERY_ORDER_TAG(16,RocketMqTagEnum.DELIVERY_ORDER,"mabang_delivery_order_tag"),
    /**
     * 马帮退款订单tag
     */
    MABANG_REFUND_ORDER_TAG(17,RocketMqTagEnum.REFUND_ORDER,"mabang_refund_order_tag"),
    /**
     * 马帮退货订单tag
     */
    MABANG_RETURN_ORDER_TAG(18,RocketMqTagEnum.RETURN_ORDER,"mabang_return_order_tag"),
    /**
     * 马帮店铺信息tag
     */
    MABANG_SHOP_INFO_TAG(19, RocketMqTagEnum.SHOP_INFO,"mabang_shop_info_tag"),
    /**
     * 马帮sku信息tag
     */
    MABANG_SKU_INFO_TAG(20, RocketMqTagEnum.SKU_INFO,"mabang_sku_info_tag"),

    /**
     * 马帮组合sku信息tag
     */
    MABANG_SKU_COMBO_INFO_TAG(21, RocketMqTagEnum.SKU_INFO,"mabang_sku_combo_info_tag"),

    /**
     * 马帮加工sku信息tag
     */
    MABANG_SKU_MACHINING_INFO_TAG(22, RocketMqTagEnum.SKU_INFO,"mabang_sku_machining_info_tag"),

    /**
     * 马帮调拨发货tag
     */
    MABANG_SHIPMENT_TAG(23,RocketMqTagEnum.SHIPMENT,"mabang_shipment_tag"),

    /**
     * 马帮发货单tag
     */
    MABANG_FBA_DELIVERY_TAG(24,RocketMqTagEnum.FBA_DELIVERY,"mabang_fba_delivery_tag"),

    /**
     * B2C销售出库单到TAG
     */
    KINGDEE_B2C_SO_OUTSTOCK_TAG(25,RocketMqTagEnum.SYNC_WMS,"kingdee_b2c_so_outatock_to_task_tag"),

    /**
     * 金蝶退货单保存到dmp_sync_task同步任务表
     */
    KINGDEE_REFUND_ORDER_TO_TASK_TAG(26,RocketMqTagEnum.SYNC_WMS,"kingdee_refund_order_to_task_tag"),

    /**
     * 汇率管理tag
     */
    KINGDEE_EXCHANGE_RATE_TAG(27,RocketMqTagEnum.SYNC_DMP,"kingdee_exchange_rate_tag"),

    /**
     * 领星店铺信息tag
     */
    LX_SHOP_INFO_TAG(28, RocketMqTagEnum.SHOP_INFO,"lx_shop_info_tag"),

    /**
     * 领星FBA货件信息tag
     */
    LX_FBA_SHIPMENT_RECEIVE_TAG(29, RocketMqTagEnum.FBA_SHIPMENT,"lx_fba_shipment_receive_tag"),

    /**
     * 虚拟仓明细信息tag
     */
    WMS_VIRTUAL_DETAIL_MSG_TAG(30,RocketMqTagEnum.SYNC_WMS,"wms_virtual_detail_msg_tag"),

    /**
     * 微信订阅消息tag DMP_WECHAT_SUBSCRIBE_MSG
     */
    DMP_WECHAT_SUBSCRIBE_MSG_TAG(31,RocketMqTagEnum.WECHAT_SUBSCRIBE,"dmp_wechat_subscribe_msg_tag"),

    //---------------------------------金蝶数据同步code从1001开始------------------------------------------------------------------------------------------

    /**
     * 产品信息同步金蝶
     */
    KINGDEE_PRODUCT_DETAIL_TAG(1001, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_product_detail_tag"),

    /**
     * BOM管理同步金蝶
     */
    KINGDEE_BOM_INFO_TAG(1002, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_bom_info_tag"),

    /**
     * 辅助资料同步金蝶（产品分类、）
     */
    KINGDEE_ASSISTANT_DATA_TAG(1003, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_assistant_data_tag"),

    /**
     * 系统用户同步金蝶
     */
    KINGDEE_SYS_USER_INFO_TAG(1004, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_sys_user_info_tag"),

    /**
     * 采购订单同步金蝶
     */
    KINGDEE_PURCHASE_ORDER_TAG(1005, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_order_tag"),

    /**
     * 采购价目表
     */
    KINGDEE_PURCHASE_PRICE_TAG(1006, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_price_tag"),

    /**
     * 采购退货单同步金蝶
     */
    KINGDEE_PURCHASE_RETURN_ORDER_TAG(1007, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_return_order_tag"),

    /**
     * 采购调价表
     */
    KINGDEE_PURCHASE_PRICE_CHANGE_TAG(1008, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_price_change_tag"),

    /**
     * 采购入库单同步金蝶
     */
    KINGDEE_PURCHASE_STOCK_IN_TAG(1009, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_stock_in_tag"),

    /**
     * 供应商同步金蝶
     */
    KINGDEE_SUPPLIER_TAG(1010, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_supplier_tag"),

    /**
     * 仓库同步金蝶
     */
    KINGDEE_WAREHOUSE_TAG(1011, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_warehouse_tag"),

    /**
     * 部门同步金蝶
     */
    KINGDEE_SYS_DEPARTMENT_TAG(1012, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_sys_department_tag"),



    /**
     * 直接调拨单同步金蝶
     */
    KINGDEE_TRANSFER_INFO_TAG(1013, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_transfer_info_tag"),

    /**
     * 客户信息同步金蝶
     */
    KINGDEE_CUSTOMER_TAG(1014, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_customer_tag"),

    /**
     * 其他出库单同步金蝶
     */
    KINGDEE_OTHER_OUTSTOCK_TAG(1015, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_other_outstock_tag"),

    /**
     * 其他入库单同步金蝶
     */
    KINGDEE_OTHER_INSTOCK_TAG(1016, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_other_instock_tag"),

    /**
     * 客户分组同步金蝶
     */
    KINGDEE_CUSTOMER_GROUP_TAG(1017, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_customer_group_tag"),

    /**
     * 加工单同步金蝶
     */
    KINGDEE_MACHINE_INFO_TAG(1018, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_machine_info_tag"),

    /**
     *销售订单同步金蝶
     */
    KINGDEE_SO_INFO_TAG(1019, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_so_info_tag"),

    /**
     *销售出库同步金蝶
     */
    KINGDEE_SO_OUTSTOCK_TAG(1020, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_so_outstock_tag"),

    /**
     *销售退货单同步金蝶
     */
    KINGDEE_SO_RETURN_TAG(1021, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_so_return_tag"),

    /**
     *销售变更订单同步金蝶
     */
    KINGDEE_SO_CHANGE_TAG(1022, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_so_change_tag"),

    /**
     *客户联系人同步金蝶
     */
    KINGDEE_CUSTOMER_CONTACT_TAG(1023, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_customer_contact_tag"),

    /**
     *委外订单同步金蝶
     */
    KINGDEE_SUBCONTRACT_ORDER_TAG(1024, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_subcontract_order_tag"),

    /**
     *委外变更单同步金蝶
     */
    KINGDEE_SUBCONTRACT_CHANGE_TAG(1025, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_subcontract_change_tag"),

    /**
     *盘盈单同步到金蝶
     */
    KINGDEE_STOCKTAKING_PROFIT_TAG(1026, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_stocktaking_profit_tag"),

    /**
     *盘亏单同步到金蝶
     */
    KINGDEE_STOCKTAKING_LOSS_TAG(1027, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_stocktaking_loss_tag"),

    /**
     *采购收货单同步到金蝶
     */
    KINGDEE_PO_RECEIVE_TAG(1028, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_po_receive_tag"),

    /**
     * 采购订单变更单同步金蝶
     */
    KINGDEE_PURCHASE_CHANGE_TAG(1029, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_purchase_change_tag"),

    /**
     * 委外发料单同步金蝶
     */
    KINGDEE_SUBCONTRACT_ISSUE_TAG(1030, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_subcontract_issue_tag"),

    /**
     * 岗位同步金蝶
     */
    KINGDEE_SYS_POST_TAG(1031, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_sys_post_tag"),

    /**
     * 员工任岗同步金蝶
     */
    KINGDEE_SYS_USER_POST_TAG(1032, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_sys_user_post_tag"),

    /**
     * 业务员同步金蝶
     */
    KINGDEE_OPERATOR_TAG(1033, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_operator_tag"),

    /**
     * 分步式调出单同步金蝶
     */
    KINGDEE_TRANSFER_OUT_TAG(1034, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_transfer_out_tag"),

    /**
     * 分步式调入单同步金蝶
     */
    KINGDEE_TRANSFER_IN_TAG(1035, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_transfer_in_tag"),

    /**
     * 对账单同步金蝶
     */
    KINGDEE_PO_RECONCILIATION_TAG(1036, RocketMqTagEnum.SYNC_KINGDEE,"kingdee_po_reconciliation_tag"),
    //-----------------------------dmp数据更新编码从2001开始---------------------------------------------------------------

    /**
     * 谷仓库存业务处理实现
     */
    GC_STOCK_INBOUND_ORDER_TAG(2001,RocketMqTagEnum.STOCK,"gc_stock_inbound_order_tag"),

    /**
     * 拉取艾姆勒入库单
     */
    IML_STOCK_INBOUND_ORDER_TAG(2002,RocketMqTagEnum.STOCK,"iml_stock_inbound_order_tag"),
    /**
     * 同步产品sku变更新老品
     */
    SYNC_DMP_PRODUCT_LISTING_TAG(2005,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_listing_tag"),
    /**
     * 获取上市时间
     */
    GET_DMP_PRODUCT_LISTING_TAG(2006,RocketMqTagEnum.SYNC_DMP,"get_dmp_product_listing_tag"),

    /**
     * 店铺变更负责人
     */
    SHOP_INFO_CHANGE_CHARGE_TAG(2007, RocketMqTagEnum.SYNC_DMP,"shop_info_change_charge_tag"),
    /**
     * 店铺变更部门
     */
    SHOP_INFO_CHANGE_DEPT_TAG(2008, RocketMqTagEnum.SYNC_DMP,"shop_info_change_dept_tag"),
    /**
     * 汇率更新
     */
    CHANGE_CURRENCY_TAG(2009, RocketMqTagEnum.SYNC_DMP,"change_currency_tag"),
    /**
     * 订单审核通过后同步dmp
     */
    APPROVED_SO_INFO_ORDER_TO_DMP_TAG(2011, RocketMqTagEnum.SYNC_DMP,"approved_so_info_order_to_dmp_tag"),
    /**
     * 订单审核通过后同步dmp
     */
    APPROVED_SO_OUTSTOCK_ORDER_TO_DMP_TAG(2012, RocketMqTagEnum.SYNC_DMP,"approved_so_outstock_order_to_dmp_tag"),
    /**
     * 订单审核通过后同步dmp
     */
    APPROVED_RETURN_ORDER_TO_DMP_TAG(2010, RocketMqTagEnum.SYNC_DMP,"approved_return_order_to_dmp_tag"),
    /**
     * b2c订单同步dmp
     */
    SO_B2C_TO_DMP_TAG(2013, RocketMqTagEnum.SYNC_DMP,"so_b2c_to_dmp_tag"),
    /**
     * b2c发货单同步dmp
     */
    SO_B2C_DELIVERY_TO_DMP_TAG(2014, RocketMqTagEnum.SYNC_DMP,"so_b2c_delivery_to_dmp_tag"),
    //-----------------------------plm数据更新编码从3001开始---------------------------------------------------------------
    /**
     * 修改上市时间
     */
    PRODUCT_LISTING_UPDATE_TAG(3001, RocketMqTagEnum.SKU_INFO,"product_listing_update_tag"),

    /**
     * 同步产品sku到中台wms仓储系统
     */
    SYNC_WMS_PRODUCT_SKU_TAG(3002,RocketMqTagEnum.SYNC_WMS,"sync_wms_product_sku_tag"),

    /**
     * 同步产品sku销售信息到wms仓储系统
     */
    SYNC_WMS_PRODUCT_SKU_SALE_TAG(3003,RocketMqTagEnum.SYNC_WMS,"sync_wms_product_sku_sale_tag"),

    /**
     * 同步产品信息到wms仓储系统
     */
    SYNC_WMS_PRODUCT_INFO_TAG(3004,RocketMqTagEnum.SYNC_WMS,"sync_wms_product_info_tag"),

    /**
     * 同步产品信息到中台dmp
     */
    SYNC_DMP_PRODUCT_INFO_TAG(3005,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_info_tag"),

    /**
     * 同步产品sku到中台dmp
     */
    SYNC_DMP_PRODUCT_SKU_TAG(3006,RocketMqTagEnum.SYNC_DMP,"sync_dmp_product_sku_tag"),

    /**
     * 同步产品信息到scm
     */
    SYNC_SCM_PRODUCT_INFO_TAG(3007,RocketMqTagEnum.SYNC_DMP,"sync_scm_product_info_tag"),

    /**
     * 同步产品sku到scm
     */
    SYNC_SCM_PRODUCT_SKU_TAG(3008,RocketMqTagEnum.SYNC_DMP,"sync_scm_product_sku_tag"),
    //-----------------------------SCM编码从4001开始------------------------------------------------------------------

    /**
     * 同步采购订单到WMS
     */
    SYNC_WMS_PURCHASE_ORDER_TAG(4001,RocketMqTagEnum.SYNC_SCM,"sync_wms_purchase_order_tag"),

    /**
     * 同步采购订单详情到WMS
     */
    SYNC_WMS_PURCHASE_ORDER_DETAIL_TAG(4002,RocketMqTagEnum.SYNC_SCM,"sync_wms_purchase_order_detail_tag"),

    /**
     * 同步采购订单详情到SRM
     */
    SYNC_SRM_PURCHASE_ORDER_DETAIL_TAG(4004,RocketMqTagEnum.SYNC_SCM,"sync_srm_purchase_order_detail_tag"),
    /**
     * 同步采购变更单信息到SRM
     */
    SYNC_SRM_PURCHASE_ORDER_DETAIL_INFO_TAG(4005,RocketMqTagEnum.SYNC_SCM,"sync_srm_purchase_order_detail_info_tag"),
    /**
     * 同步采购订单供应商信息到WMS
     */
    SYNC_WMS_PURCHASE_ORDER_SUPPLIER_TAG(4003,RocketMqTagEnum.SYNC_SCM,"sync_wms_purchase_order_supplier_tag"),


    //---------------------------------WMS 数据更新从6001开始---------------------------------------------------------------------------------

    /**
     * 金蝶同步销售出库单到WMS
     */
    SYNC_KINGDEE_SO_OUTSTOCK_TAG(6001,RocketMqTagEnum.SYNC_WMS,"sync_kingdee_so_outatock_tag"),

    /**
     * 金蝶同步直接调拨单到WMS
     */
    SYNC_KINGDEE_TRANSFER_INFO_TO_WMS_TAG(6002,RocketMqTagEnum.SYNC_WMS,"sync_kingdee_transfer_info_to_wms_tag"),

    /**
     * 马帮同步FBA发货单到到WMS加工单
     */
    SYNC_MABANG_FBA_DELIVERY_TO_WMS_TAG(6003,RocketMqTagEnum.SYNC_WMS,"sync_mabang_fba_delivery_to_wms_tag"),

    /**
     * 金蝶退货订单同步到WMS退货入库单
     */
    SYNC_KINGDEE_RETURN_ORDER_TO_WMS_TAG(6004,RocketMqTagEnum.SYNC_WMS,"sync_kingdee_return_order_to_wms_tag"),

    /**
     * 金蝶汇率列表同步到DMP
     */
    SYNC_KINGDEE_EXCHANGE_RATE_TO_WMS_TAG(6005,RocketMqTagEnum.SYNC_DMP,"sync_kingdee_exchange_rate_to_wms_tag"),

    /**
     * 异步合并组包发货
     */
    ASYNC_MERGE_PACKAGE_DELIVERY_TAG(6006,RocketMqTagEnum.SYNC_WMS,"async_merge_package_delivery_tag"),
    /**
     * 金蝶同步销售出库单到WMS
     */
    SYNC_WDT_SO_OUT_STOCK_TAG(6007,RocketMqTagEnum.SYNC_WMS,"sync_wdt_so_out_stock_tag"),
    //---------------------------------马帮数据同步code从7001开始------------------------------------------------------------------------------------------

    /**
     * ERP直接调拨单同步DMP出入库
     */
    ERP_DMP_TRANSFER_INFO_TAG(7001, RocketMqTagEnum.SYNC_DMP,"erp_dmp_transfer_info_tag"),

    /**
     * DMP出入库同步到马帮
     */
    MABANG_INOUT_STOCK_TAG(7002, RocketMqTagEnum.SYNC_MABANG,"mabang_inout_stock_tag"),

    /**
     * ERP加工单同步DMP出入库
     */
    ERP_DMP_MACHINE_INFO_TAG(7003, RocketMqTagEnum.SYNC_DMP,"erp_dmp_machine_info_tag"),

    //-----------------------------亚马逊报告编码从8001开始------------------------------------------------------------------

    /**
     * 亚马逊创建报告Tag
     */
    AMZ_REPORT_CREATE_TAG(8001, RocketMqTagEnum.AMZ_REPORT,"amz_report_create_tag"),
    /**
     * 亚马逊查询报告Tag
     */
    AMZ_REPORT_QUERY_TAG(8002, RocketMqTagEnum.AMZ_REPORT,"amz_report_query_tag"),

    /**
     * 亚马逊下载报告Tag
     */
    AMZ_REPORT_DOWNLOAD_TAG(8003, RocketMqTagEnum.AMZ_REPORT,"amz_report_download_tag"),

    /**
     * 亚马逊解析报告Tag
     */
    AMZ_REPORT_PARSE_TAG(8004, RocketMqTagEnum.AMZ_REPORT,"amz_report_parse_tag"),

    /**
     * 亚马逊不创建直接查询报告Tag
     */
    AMZ_REPORT_DIRECT_QUERY_TAG(8005, RocketMqTagEnum.AMZ_REPORT,"amz_report_direct_query_tag"),



    //-----------------------------公共信息编码从9001开始------------------------------------------------------------------

    /**
     * 消息通知，可以不同的业务使用不同的tag
     */
    MSG_NOTICE_TAG(9001,RocketMqTagEnum.MSG_NOTICE,"msg_notice_default_tag"),

    /**
     * 预警通知
     */
    MSG_WARN_TAG(9002,RocketMqTagEnum.MSG_WARN,"msg_warn_tag"),

    /**
     * DMP同步任务消同步状态信息回调
     */
    DMP_SYNC_TASK_CALLBACK_TAG(9003,RocketMqTagEnum.SYNC_DMP,"dmp_sync_task_callback_tag"),

    /**
     * 同步系统操作日志
     */
    SYNC_ERP_LOG_TO_SYS_TAG(9004, RocketMqTagEnum.SYS_LOG,"sync_erp_log_to_sys_tag"),


    //-----------------------------TMS编码从10001开始------------------------------------------------------------------
    /**
     * 异步获取平台打印面单标签
     */
    ASYNC_GET_PLATFORM_LABEL_TAG(10001, RocketMqTagEnum.SYNC_TMS,"async_get_platform_label_tag"),
    /**
     * 异步更新物流获取记录
     */
    ASYNC_GET_TRACK123_LOGISTICS_TRACK(10002, RocketMqTagEnum.SYNC_TMS, "async_get_logistics_track"),

    //-----------------------------旺店通编码从11001开始------------------------------------------------------------------
    /**
     * 同步产品资料到旺店通
     */
    WDT_PRODUCT_DETAIL_TAG(11001, RocketMqTagEnum.SYNC_WANGDIAN,"wdt_product_detail_tag"),
    WDT_DELIVERY_ORDER_TAG(11002, RocketMqTagEnum.DELIVERY_ORDER,"wdt_delivery_order_tag"),
    WDT_DELIVERY_ORDER_WMS_TAG(11002, RocketMqTagEnum.SYNC_WMS,"wdt_delivery_order_wms_tag"),

    WDT_OTHER_IN_STOCK_TAG(11003, RocketMqTagEnum.SYNC_WANGDIAN, "wdt_other_in_stock_tag"),
    WDT_OTHER_OUT_STOCK_TAG(11004, RocketMqTagEnum.SYNC_WANGDIAN, "wdt_other_out_stock_tag"),
    WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL_TAG(11005, RocketMqTagEnum.SYNC_WANGDIAN, "wdt_virtual_allocation_handle_detail_tag"),

    /**
     * 同步产品资料到领星
     */
    LINGXING_PRODUCT_DETAIL_TAG(11001, RocketMqTagEnum.SYNC_LINGXING,"lingxing_product_detail_tag"),
    /**
     * 速帝云通用推送
     */
    SDY_GENERAL_PUSH_TAG(11006, RocketMqTagEnum.SYNC_WANGDIAN, "sdy_general_push_tag"),

    //-----------------------------workflow 从12001开始------------------------------------------------------------------
    /**
     * workflow 同步飞书流程实例
     */
    WORKFLOW_SYNC_FS_INSTANCE_TAG(12001, RocketMqTagEnum.SYNC_WORKFLOW, "workflow_sync_fs_instance_tag"),



    //-----------------------------sys 从13001开始------------------------------------------------------------------
    /**
     * sys 三方通知推送记录
     */
    SYS_SEND_THIRD_NOTICE_TAG(13001, RocketMqTagEnum.SYNC_SYS, "sys_send_third_notice_tag"),

    SYS_RECEIVE_DDL_TO_MQ_TAG(13002, RocketMqTagEnum.SYNC_SYS, "sys_receive_ddl_to_mq_tag"),


    ;
    public static final String SALES_ORDER = "sales";
    public static final String DELIVERY_ORDER = "delivery";
    public static final String REFUND_ORDER = "refund";
    public static final String RETURN_ORDER = "return";
    public static final String SHOP_INFO = "shop";

    public static final String SKU_INFO = "sku";

    public static final String SYNC_KINGDEE = "sync_kingdee";

    public static final String STOCK = "stock";

    public static final String SYNC_DMP = "sync_dmp";

    public static final String MSG_NOTICE = "msg_notice";

    public static final String SYNC_WMS = "sync_wms";

    public static final String SYNC_SCM = "sync_scm";

    public static final String SYNC_OMS = "sync_oms";

    public static final String SYNC_TMS = "sync_tms";

    public static final String SYNC_SYS = "sync_sys";

    public static final String SYNC_WORKFLOW = "sync_workflow";

    public static final String MSG_WARN = "msg_warn";

    public static final String TRANSFER = "transfer";

    public static final String SYNC_MABANG = "sync_mabang";

    public static final String SHIPMENT = "shipment";

    public static final String FBA_DELIVERY = "fba_delivery";

    public static final String SYS_LOG = "sys_log";

    public static final String AMZ_REPORT = "amz_report";

    public static final String FBA_SHIPMENT = "fba_shipment";

    public static final String SYNC_WANGDIAN = "sync_wangdian";

    public static final String SYNC_LINGXING = "sync_lingxing";

    public static final String WECHAT_SUBSCRIBE = "wechat_subscribe";

    private Integer code;

    private String type;

    private String name;

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    RocketMqTagEnum(Integer code, String type, String name) {
        this.code = code;
        this.type = type;
        this.name = name;
    }

    public static List<RocketMqTagEnum> listByType(String type) {
        return Arrays.stream(values()).filter(value -> value.getType().equals(type))
                .collect(Collectors.toList());
    }

    public static RocketMqTagEnum getByCode(Integer code) {
        return Arrays.stream(values())
                .filter(value -> value.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getTagStrByType(String type){
        List<RocketMqTagEnum> tagList = listByType(type);
        if (CollectionUtil.isEmpty(tagList)){
            return "";
        }
        return tagList.stream().map(RocketMqTagEnum::getName).collect(Collectors.joining("||"));
    }

    public static void main(String[] args) {
        String tagStrByType = getTagStrByType(SKU_INFO);

        log.info("tagStrByType = " + tagStrByType);
    }


}
