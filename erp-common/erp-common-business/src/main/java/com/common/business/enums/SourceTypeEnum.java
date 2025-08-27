package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public enum SourceTypeEnum {
    SELF_ADD("selfAdd", "手动新增",""),
    AUTO_ADD("autoAdd", "自动新增",""),



    //SCM
    PURCHASE_ORDER("purchaseOrder", "采购订单","purchase_order"),
    PURCHASE_APPLICATION("purchaseApplication", "采购申请单","purchase_application"),
    SUBCONTRACT_ORDER("subcontractOrder", "委外订单","subcontract_order"),
    SUBCONTRACT_CHANGE("subcontractChange", "委外变更","subcontract_change"),
    PURCHASE_PRICE_CHANGE("purchasePriceChange", "采购价变更","purchase_price_change"),
    SALES_DEMAND("salesDemand", "备货申请","sales_demand"),
    PURCHASE_CHANGE("purchaseChange", "采购变更","purchase_change"),
    PURCHASE_PRICE("purchasePrice", "采购价目表","purchase_price"),
    SUPPLIER("supplier", "供应商列表","supplier"),
    CFG_SUPPLIER_SALES("cfgSupplierSales", "销量设置","cfg_supplier_sales"),
    CONTRACT_INFO("contractInfo", "合同管理","contract_info"),
    SUPPLIER_PHASE("supplierPhase", "供应商阶段审核列表","supplier_phase"),



    //WMS
    PO_INSTOCK("poInstock", "采购入库单","po_instock"),
    QC_INFO("qcInfo", "质检单","qc_info"),
    PO_RETURN("poReturn", "采购退货单","po_return"),
    PO_RETURN_DETAIL("poReturnDetail", "采购退货单详情","po_return_detail"),
    PO_RECEIVE("poReceive", "仓库签收单","po_receive"),
    TRANSFER_APPLICATION("transferApplication", "调拨申请单","transfer_application"),
    SO_RETURN_NOTICE("soReturnNotice", "销售退货通知单","so_return_notice"),
    SO_RETURN_RECEIVE("soReturnReceive", "销售退货签收单","so_return_receive"),
    SO_RETURN_INSTOCK("soReturnInstock", "销售退货入库单","so_return_instock"),
    SO_DELIVERY_NOTICE("soDeliveryNotice", "销售发货通知单","so_delivery_notice"),
    SO_DELIVERY_NOTICE_CHANGE("soDeliveryNoticeChange", "销售发货通知变更单","so_delivery_notice_change"),
    TRANSFER_OUT("transferOut", "分布式调出单","transfer_out"),
    TRANSFER_IN("transferIn", "分布式调入单","transfer_in"),
    TRANSFER_INFO("transferInfo", "直接调拨单","transfer_info"),
    MACHINE_INFO("machineInfo", "加工单","machine_info"),
    SO_OUTSTOCK("soOutstock", "销售出库单","so_outstock"),
    STOCKTAKING_TASK("stocktakingTask", "盘点任务单","stocktaking_task"),
    STOCKTAKING_PLAN("stocktakingPlan", "盘点计划单","stocktaking_plan"),
    OTHER_INSTOCK("otherInstock", "其他入库","other_instock"),
    WDT_OTHER_INSTOCK("wdtOtherInstock", "其他入库","other_instock"),
    OTHER_OUTSTOCK("otherOutstock", "其他出库","other_outstock"),
    WDT_OTHER_OUTSTOCK("wdtOtherOutstock", "其他出库","other_outstock"),
    WAREHOUSE("warehouse", "仓库","warehouse"),
    SDY_WAREHOUSE("sdy_warehouse", "仓库","warehouse"),
    SO_B2C_DELIVERY("soB2cDelivery", "B2C发货单","so_b2c_delivery"),
    SO_B2C_DELIVERY_INTERCEPT("soB2cDeliveryIntercept", "B2C发货单拦截","so_b2c_delivery_intercept"),
    PLATFORM_SO_OUT_STOCK("platformSoOutStock", "平台销售出库单",""),
    PACKING_TASK("packingTask", "装箱任务",""),


    STOCKTAKING_PROFIT_LOSS("stocktakingTaskProfitLoss", "盘盈盘亏单","stocktaking_profit_loss"),
    STOCKTAKING_LOSS("stocktakingTaskLoss", "盘亏单","stocktaking_profit_loss"),
    STOCKTAKING_PROFIT("stocktakingTaskProfit", "盘盈单","stocktaking_profit_loss"),
    FBA_SHIPMENT("fbaShipment", "FBA货件","fba_shipment"),
    FBA_SHIPMENT_DETAIL("fbaShipmentDetail", "FBA货件明细","fba_shipment_detail"),
    FIRST_MILE_DELIVERY("firstMileDelivery", "头程发货单", "first_mile_delivery"),
    //当前仓可用减少，中转仓冻结增加
    FIRST_MILE_DELIVERY_TRANSFER_TO_THIRD("firstMileDeliveryTransferToThird", "头程发货单-中转（三方仓发三方仓）", "first_mile_delivery"),
    FIRST_MILE_DELIVERY_TO_ULANZI("firstMileDeliveryToUlanzi", "头程发货单（发货仓->优蓝子中转仓）","first_mile_delivery"),
    FIRST_MILE_DELIVERY_TO_THIRD("firstMileDeliveryToThird", "头程发货单中转（三方仓发三方仓）","first_mile_delivery"),
    FIRST_MILE_DELIVERY_FROM_ULANZI("firstMileDeliveryFromUlanzi", "头程发货单（优蓝子中转仓->目的在途仓）","first_mile_delivery"),
    FBA_INVENTORY("fbaInventory", "FBA仓库","fba_inventory"),
    DELIVERY_PLAN("deliveryPlan", "发货计划","wms_delivery_plan"),
    OVERSEAS_INBOUND("overseasInbound", "海外仓入库单","overseas_warehouse_inbound"),
    REQUISITION_APPLICATION("requisitionApplication", "要货申请","requisition_application"),
    REQUISITION_APPLICATION_CHANGE("requisitionApplicationChange", "要货申请变更单","requisition_application_change"),
    REQUISITION_APPLICATION_HANDLE("requisitionApplicationHandle", "要货申请(处理)","requisition_application"),
    REQUISITION_APPLICATION_FINISH("requisitionApplicationFinish", "要货申请(完成)","requisition_application"),

    SUBCONTRACT_ISSUE("subcontractIssue", "委外发料单","subcontract_issue"),
    SUBCONTRACT_RETURN("subcontractReturn", "委外退料单","subcontract_return"),
    WAREHOUSE_AREA_INFO("warehouseAreaInfo", "库区","warehouse_area_info"),
    VIRTUAL_WAREHOUSE_ALLOCATION("virtualWarehouseAllocation", "分货单","virtual_warehouse_allocation"),
    PICKING_LISTS_ADD("pickingListsAdd", "拣货单新增","picking_lists_add"),
    PICKING_LISTS_SUBTRACT("pickingListsSubtract", "拣货单减少","picking_lists_subtract"),
    PICKING_LISTS("pickingLists", "拣货单","picking_lists"),
    QC_NOTICE("qcNotice", "质检通知单","qc_notice"),
    WAREHOUSE_LOCATION_REPLENISH("warehouseLocationReplenish", "仓位补货","warehouse_location_replenish"),
    VIRTUAL_ADJUST("virtualAdjust", "虚拟库存调整","virtual_adjust"),

    //OMS
    SO_RETURN("soReturn", "销售退货订单","so_return"),
    SO_B2C_RETURN("soB2cReturn", "B2c销售退货订单","so_return"),
    SO_INFO("soInfo", "B2B销售订单","so_info"),
    SO_INFO_TRANSFER_INFP("soInfoTransferInfo", "B2B销售订单(中转调拨)","so_info"),
    SO_CHANGE("soChange", "销售变更单","so_change"),
    CUSTOMER_INFO( "customerInfo", "客户表","customer_info"),
    CUSTOMER_ADDRESS( "customerAddress", "客户地址表","customer_address"),
    SDY_CUSTOMER_INFO( "sdy_customerInfo", "客户表","customer_info"),
    CUSTOMER_B2B_CHANGE_SELLER( "customerB2bChangeSeller", "B2B客户表变更销售员","customer_b2b_seller_change"),
    SO_B2C( "soB2c", "B2C销售订单","so_b2c"),
    TIK_TOK_FULLY( "TikTokFully", "TikTok全托管","so_b2c"),
    SO_MULTI_CHANNEL( "soMultiChannel", "多渠道订单","so_multi_channel"),
    CUSTOMER_B2C( "customerB2c", "B2C客户表","customer_b2c"),
    SHOP( "shop", "店铺","shop_info"),
    CUSTOMER_CONTACT( "customerContact", "客户联系人","customer_contact"),
    CUSTOMER_GROUP( "customerGroup", "客户分组","customer_group"),
    LISTING_INFO( "listingInfo", "产品信息","listing_info"),
    SDY_SKU_MAPPING( "sdy_skuMapping", "sku映射","sku_mapping"),
    CFG_VAT_INVOICE( "cfgVatInvoice", "VAT发票设置","cfg_vat_invoice"),
    SO_PRICE( "soPrice", "销售价目表","so_price"),
    SO_PRICE_CHANGE( "soPriceChange", "销售调价表","so_price_change"),

    CAINIAO_LISTING( "cainiao_listing", "菜鸟仓listing","cainiao_listing"),
    CAINIAO_SO_RETURN_INSTOCK("cainiaoSoReturnInstock", "菜鸟仓退货入库单","so_return_instock"),

    //SRM
    DELIVERY_ORDER( "deliveryOrder", "送货单","delivery_order"),
    PO_RECONCILIATION( "poReconciliation", "对账单","po_reconciliation"),



    //Kingdee
    SAL_RETURNSTOCK("SAL_RETURNSTOCK", "金蝶销售退货单",""),
    SAL_OUTSTOCK("SAL_OUTSTOCK", "金蝶销售出库单",""),
    STK_TRANSFERDIRECT("STK_TransferDirect", "金蝶直接调拨单",""),
    BD_RATE("BD_Rate", "汇率列表",""),



    // PLM
    PRODUCT_BOM_INFO("productBomInfo", "BOM管理","product_bom_info"),
    SDY_PRODUCT_BOM_INFO("sdy_productBomInfo", "BOM管理","product_bom_info"),
    PRODUCT_COMBINATION("productCombination", "组合产品",""),

    PRODUCT_DETAIL("productDetail", "产品管理","product_detail"),
    SDY_PRODUCT_DETAIL("sdy_productDetail", "产品管理","product_detail"),
    WDT_PRODUCT_DETAIL("wdtProductDetail", "产品管理","product_detail"),
    PROJECT_TASK("projectTask", "任务列表","project_task"),
    PRODUCT_CHANGE("productChange", "变更管理","product_change"),
    BASIC_CATEGORY("basicCategory", "产品分类","basic_category"),
    APPLICATION_CATEGORY("applicationCategory", "应用分类","application_category"),
    PRODUCT_LOGISTICS("ProductLogistics", "物流产品","product_logistics"),
    MOULD_INFO("mouldInfo", "模具管理","mould_info"),

    LX_PRODUCT_DETAIL("lx_productDetail", "产品管理","product_detail"),


    //SYS
    SYS_DEPARTMENT("sysDepartment", "部门","sys_department"),
    SYS_USER_INFO("sysUserInfo", "用户","sys_user_info"),
    SYS_POST("sysPost", "岗位","kingdee_post"),
    SYS_USER_POST("sysUserPost", "员工任岗","kingdee_user_ref_post"),
    KINGDEE_OPERATOR("kingdeeOperator", "金蝶业务员","kingdee_operator_ref_post"),
    GLOBAL_AREA("globalArea", "区域","dict_global_area"),
    COUNTRY("country", "国家","dict_country"),
    PROVINCE_CITY("provinceCity", "省市","dict_city"),

    //TMS
    TMS_B2C_DECLARE_RECONCILIATION("tmsB2cDeclareReconciliation", "B2c报关对账单","tms_b2c_declare_reconciliation"),
    TRANSFER_DECLARE("transferDeclare", "中转报关","transfer_declare"),
    DICT_BASIC("dictBasic", "字典","dict_basic"),
    TMS_CFG_COST("tmsCfgCost", "费用管理配置","tms_cfg_cost"),

    LOGISTICS_BILL_COST("logisticsBillCost", "自发货物流费用","logistics_bill_cost"),
    FIRST_MILE_LOGISTICS_BILL_COST("firstMileLogisticsBillCost", "头程物流费用","logistics_bill_cost"),
    LAST_MILE_LOGISTICS_BILL_COST("lastMileLogisticsBillCost", "尾程物流费用","logistics_bill_cost"),


    LOGISTICS_BILL("logisticsBill", "物流单","logistics_bill"),
    TMS_FIRST_MILE_RECONCILIATION("tmsFirstMileReconciliation", "头程对账单","tms_first_mile_reconciliation"),

    SMALL_BAG_COST_ALLOCATION("smallBagCostAllocation", "小包费用分摊","small_bag_cost_allocation"),
    TRANSFER_DECLARE_COST_ALLOCATION("transferDeclareCostAllocation", "中转费用分摊","transfer_declare_cost_allocation"),
    FIRST_MILE_COST_ALLOCATION("firstMileCostAllocation", "头程费用分摊","first_mile_cost_allocation"),
    PRODUCT_REGISTRATION("productRegistration", "备案通知","product_registration"),



    //MRP
    REPLENISHMENT_SUGGESTION("replenishmentSuggestion", "补货建议","replenishment_suggestion"),
    DELIVERY_SUGGESTION("deliverySuggestion", "发货建议","delivery_suggestion"),
    PURCHASE_SUGGESTION("purchaseSuggestion", "采购建议","purchase_suggestion"),
    PURCHASE_SUGGESTION_MERGE("purchaseSuggestionMerge", "采购建议(合并)","purchase_suggestion_merge"),


    //Mabang
    MABANG_FBA_DELIVERY("MB_FBA_DELIVERY", "马帮FBA发货单",""),
    LOGISTICS_SUPPLIER("logisticsSupplier","物流商",""),
    LOGISTICS_WAREHOUSE("logisticsWarehouse","物流仓库",""),
    //物流系统操作方式
    LOGISTICS_CREATE_ORDER("createOrder", "物流系统创建订单","dmp_push_task"),
    LOGISTICS_CONFIRM_ORDER("confirmOrder", "物流系统确认订单","dmp_push_task"),
    LOGISTICS_UPDATE_ORDER("updateOrder", "物流系统更新订单","dmp_push_task"),
    LOGISTICS_INTERCEPT_ORDER("interceptOrder", "物流系统拦截订单","dmp_push_task"),
    LOGISTICS_QUERY_ORDER("queryOrder", "物流系统查询订单","dmp_pull_task"),
    LOGISTICS_GET_LABEL("getLabel", "物流系统获取标签","dmp_pull_task"),
    LOGISTICS_GET_TRACK("getTrack", "物流系统轨迹查询","dmp_pull_task"),
    LOGISTICS_REGISTER_TRACK("registerTrack", "物流系统注册物流单",""),
    LOGISTICS_GET_LABEL_LIST("getLabelList", "物流系统批量获取标签","dmp_pull_task"),
    LOGISTICS_GET_CHANEL_LIST("getChanelList", "物流系统批量渠道列表","dmp_pull_task"),
    LOGISTICS_CANCEL_ORDER("cancelOrder", "物流系统取消订单","dmp_push_task"),

    THIRD_WAREHOUSE("thirdWarehouse","第三方仓库",""),
    THIRD_WAREHOUSE_GET_SKU("thirdWarehouseGetSku", "第三方仓产品数据拉取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_WAREHOUSE("thirdWarehouseGetWarehouse", "第三方仓仓库数据拉取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_BASE_ADDRESS("thirdWarehouseGetBaseAddress", "第三方仓地址基础信息拉取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_INBOUND_RECEIPT("thirdWarehouseGetInboundReceipt", "第三方仓签收数据获取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_OUTBOUND_RECEIPT("thirdWarehouseGetOutboundReceipt", "第三方仓出库数据获取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_INVENTORY("thirdWarehouseGetInventory", "第三方仓库存获取","dmp_pull_task"),
    THIRD_WAREHOUSE_GET_TRANSIT_WAREHOUSE_AND_LOGISTIC("thirdWarehouseGetTransitWarehouseAndLogistic", "第三方仓中转仓及支持的物流产品基础数据获取","dmp_pull_task"),
    THIRD_WAREHOUSE_CREATE_INBOUND_BILL("thirdWarehouseCreateInboundBill", "第三方仓创建入库单","dmp_push_task"),
    THIRD_WAREHOUSE_EDIT_INBOUND_BILL("thirdWarehouseEditInboundBill", "第三方仓编辑入库单","dmp_push_task"),
    THIRD_WAREHOUSE_CANCEL_INBOUND_BILL("thirdWarehouseCancelInboundBill", "第三方仓取消入库单","dmp_push_task"),
    THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL("thirdWarehouseCreateOutboundBill", "第三方仓创建出库单","dmp_push_task"),
    THIRD_WAREHOUSE_CANCEL_OUTBOUND_BILL("thirdWarehouseCancelOutboundBill", "第三方仓取消出库单","dmp_push_task"),
    THIRD_WAREHOUSE_CALCULATE_FEE("thirdWarehouseCalculateFee", "第三方仓运费试算","dmp_push_task"),
    THIRD_WAREHOUSE_UPLOAD_FILE("thirdWarehouseUploadFile", "第三方仓上传附件","dmp_push_task"),
    THIRD_WAREHOUSE_UPLOAD_ORDER_LABEL("thirdWarehouseUploadOrderLabel", "第三方仓上传面单","dmp_push_task"),
    THIRD_WAREHOUSE_REFRESH_TOKEN("thirdWarehouseRefreshToken", "第三方仓刷新token","dmp_pull_task"),

    TRANSFER_LOGISTICS_GET_SHIPPING("transferLogisticsGetShipping", "物流报关商获取物流产品数据","dmp_pull_task"),
    TRANSFER_LOGISTICS_CREATE_ORDER("transferLogisticsCreateOrder", "物流报关商创建订单","dmp_push_task"),
    TRANSFER_LOGISTICS_CANCEL_ORDER("transferLogisticsCancelOrder", "物流报关商取消订单","dmp_push_task"),
    TRANSFER_LOGISTICS_CREATE_PRODUCT("transferLogisticsCreateProduct", "物流报关商备案产品","dmp_push_task"),
    TRANSFER_LOGISTICS_GET_ORDER_BY_CODE("transferLogisticsGetOrderByCode", "物流报关商获取单个订单","dmp_pull_task"),
    TRANSFER_LOGISTICS_GET_ALL_PRODUCT_INFO("transferLogisticsGetAllProductInfo", "物流报关商获取备案产品","dmp_pull_task"),
    TRANSFER_LOGISTICS_CREATE_INBOUND("transferLogisticsCreateInbound", "物流报关商创建入库单","dmp_push_task"),
    TRANSFER_LOGISTICS_PRINT_LABEL("transferLogisticsPrintLabel", "物流报关商打印标签","dmp_pull_task"),
    FM_DECLARE_BILL("fmDeclareBill", "头程报关单","tms_declare_bill"),
    B2B_DECLARE_BILL("b2bDeclareBill", "B2B报关单","tms_declare_bill"),


    // 亚马逊相关
    AMZ_REPORT_CONSUMER("amzReportConsumer", "亚马逊报告消费处理","amz_report_task"),
    PLATFORM_RETURN_INSTOCK("platform_return_instock", "平台仓退货入库单","dmp_third_return_inbound"),

    // 旺店通相关
    WDT_OUT_STOCK("wdt_out_stock", "旺店通销售出库单","dmp_pull_task"),
    WDT_RETURN_ORDER("wdt_return_order", "旺店通退货入库单","dmp_pull_task"),

    //旺店通
    WDT_WAREHOUSE("wdt_warehouse", "旺店通仓库基础数据", "dmp_pull_task"),
    WDT_VIRTUAL_WAREHOUSE("wdt_virtual_warehouse", "旺店通虚拟仓基础数据", "dmp_pull_task"),
    WDT_SHOP("wdt_shop", "旺店通店铺基础数据", "dmp_pull_task"),

    QIMEN_SO_OUT_STOCK("qimen_so_out_stock", "奇门销售出库单", "dmp_pull_task"),
    QIMEN_RETURN_ORDER("qimen_return_order", "奇门销售退货入库单", "dmp_pull_task"),
    PILOT_APPLICATION("pilotApplication", "试产量产单", "pilot_application"),
    FIRST_MILE_ESTIMATED("first_mile_estimated", "头程暂估账单", "first_mile_estimated_bill"),
    THIRD_WAREHOUSE_RETURN_INSTOCK("third_warehouse_return_instock", "三方仓退货入库单","dmp_pull_task"),


    //速帝云
    SDY_DELIVERY_ORDER("sdyDeliveryOrder", "速帝云配货单","so_b2c"),
    SDY_OFFLINE_ORDER("sdyOfflineOrder", "速帝云B2B订单","so_info"),
    SDY_ONLINE_ORDER("sdyOnlineOrder", "速帝云线上订单","dmp_so_info"),
    SDY_SO_OUTSTOCK("sdySoOutstock", "速帝云销售出库订单","so_outstock"),
    SDY_SO_RETURN_INSTOCK("sdySoReturnInstock", "速帝云退货入库单","so_return_instock"),
    SDY_LOGISTICS_BILL("sdyLogisticsBill", "速帝云运单","logistics_bill"),
    SDY_SELF_DELIVERY_ORDER("sdySelfDeliveryOrder", "速帝云自发货配货单","so_b2c_delivery"),
    SDY_ALIEXPRESS_DELIVERY_ORDER("sdyAliExpressDeliveryOrder", "速帝云速卖通配货单","aliexpress_delivery"),


    //售后申请
    AFTER_SALE("afterSale", "售后申请","after_sale"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    private String tableName;

    SourceTypeEnum(String type, String name, String tableName) {
        this.code = type;
        this.name = name;
        this.tableName = tableName;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public static String getName(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (sourceTypeEnum.getCode().equals(type)) {
                return sourceTypeEnum.getName();
            }
        }

        return "";
    }

    public static String getTableName(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (type.equals(sourceTypeEnum.getCode())) {
                return sourceTypeEnum.getTableName();
            }
        }
        return "";
    }

    public static SourceTypeEnum getEnum(String type) {
        for (SourceTypeEnum sourceTypeEnum : SourceTypeEnum.values()) {
            if (type.equals(sourceTypeEnum.getCode())) {
                return sourceTypeEnum;
            }
        }
        return null;
    }

    public static SourceTypeEnum getByCode(String code) {
        return Arrays.stream(SourceTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
    public static List<String> pickingLists() {
        return Arrays.asList(PICKING_LISTS_ADD.getCode(), PICKING_LISTS_SUBTRACT.getCode());
    }
}
