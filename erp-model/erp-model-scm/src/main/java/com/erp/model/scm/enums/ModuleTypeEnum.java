package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/17 11:07
 */
public enum ModuleTypeEnum {

    SALES_DEMAND("0", "备货申请单"),
    PURCHASE_APPLICATION("1", "采购申请单"),
    PURCHASE_ORDER("2", "采购订单"),
    SUPPLIER("3", "供应商"),
    PURCHASE_PRICE("4", "采购价目申请单"),
    PURCHASE_PRICE_CHANGE("5", "采购价目变更单"),
    PURCHASE_CHANGE("6", "采购变更单"),
    PO_INSTOCK("7", "采购入库单"),
    WAREHOUSE_RECEIVE("8", "收货单"),
    PURCHASE_RETURN_ORDER("9", "采购退货单"),
    QC_ORDER("10", "质检单"),
    TRANSFER_APPLICATION("11", "调拨申请单"),
    SO_DELIVERY_NOTICE("12", "发货通知单"),
    CUSTOMER("14", "客户"),
    INIT_STOCK("13", "期初库存"),
    SO_RETURN("15", "销售退货订单"),
    TRANSFER_INFO("16", "直接调拨单"),
    SO_RETURN_NOTICE("17", "销售退货通知单"),
    SO("18", "销售订单"),
    MACHINE_INFO("19", "加工单"),
    OTHER_INSTOCK("20", "其他入库单"),
    SO_RETURN_RECEIVE("21", "销售退货签收单"),
    SO_OUT_STOCK("22", "销售出库单"),
    OTHER_OUTSTOCK("23", "其他出库单"),
    SO_RETURN_INSTOCK("24", "销售退货入库单"),
    SO_CHANGE("25", "销售变更单"),
    TRANSFER_OUT("26", "分步式调出单"),
    TRANSFER_IN("27", "分步式调入单"),
    SUBCONTRACT_ORDER("28", "委外订单"),
    SUBCONTRACT_CHANGE("29", "委外变更单"),
    STOCKTAKING_TASK("30", "盘点任务单"),
    STOCKTAKING_PROFIT_LOSS("31", "盘盈盘亏单"),
    STOCKTAKING_PLAN("40", "盘点计划单"),
    SO_B2C("41", "B2C销售订单"),
    WAREHOUSE_LOCATION_MOVE_INFO("42", "仓位移动单"),
    CUSTOMER_B2C("43", "B2C客户"),
    RULE_ORDER_APPROVAL("44", "订单审核规则"),
    RULE_DELIVERY_WAREHOUSE("45", "发货仓库规则"),
    RULE_LOGISTICS("46", "物流规则"),
    FIRST_MILE_DELIVERY("47", "发货单"),
    FBA_SHIPMENT("48", "FBA货件单"),
    LOGISTICS_SUPPLIER("49", "物流商"),
    LOGISTICS_BILL("50", "物流单"),
    LOGISTICS_CHANNEL("51", "物流渠道"),
    SHIPPING_TEMPLATE("53", "运费模板"),
    LOGISTICS_BILL_COST("52", "自发货费用"),
    THIRD_WAREHOUSE("53", "第三方仓"),
    DELIVERY_PLAN("54", "发货计划"),
    REQUISITION_APPLICATION("55", "要货申请"),
    OVERSEAS_PROVIDER("56", "仓库设置"),
    OVERSEAS_WAREHOUSE_INBOUND("57", "海外仓入库单"),
    SO_B2C_DELIVERY("58", "b2c发货单"),
    SO_B2C_DELIVERY_INTERCEPT("59", "b2c发货拦截单"),
    SKU_MAPPING_RULE("60", "sku自动匹配规则"),
    SKU_MAPPING("61", "sku映射表"),
    LISTING_INFO("62", "对应平台sku 表"),
    SUPPLIER_REF_USER("63", "供应商用户关系"),
    SUBCONTRACT_ISSUE("64", "委外发料单"),
    SRM_USER("65", "SRM用户"),
    DELIVERY_ORDER("66", "发货单"),
    PO_RECONCILIATION("67", "对账单"),
    TRANSFER_LOGISTICS_CHANNEL("63", "中转报关服务商渠道"),
    TRANSFER_LOGISTICS_SUPPLIER("64", "中转报关服务商"),
    TRANSFER_DECLARE("65", "中转报关单"),
    PACKAGE_FORECAST("66", "组包预报"),
    CUSTOMER_B2B_SELLER_CHANGE("63", "b2b客户销售员变更单"),
    PRODUCT_CERTIFICATE("64", "产品认证"),
    DATA_COMPARE("68", "数据对比"),
    PRODUCT_REGISTRATION("68", "产品备案"),
    TMS_B2C_DECLARE_RECONCILIATION("69", "B2C报关账单"),
    TMS_FIRST_MILE_RECONCILIATION("70", "头程对账单"),
    CFG_FIELD_RECONCILIATION("71", "字段配置"),
    RULE_DECLARE("72", "申报规则"),
    CFG_RULE_ORDER_HANDLE("73", "订单处理规则"),
    SO_B2C_DECLARE("74", "B2C销售订单申报信息"),

    SO_MULTI_CHANNEL("75", "多渠道订单"),
    DMP_THIRD_WAREHOUSE("76", "第三方仓库"),
    DMP_THIRD_SHOP("77", "第三方店铺"),
    DMP_THIRD_MAPPING("78", "第三方映射"),
    WAREHOUSE_AREA("80", "库区"),
    PICKING_STRATEGY("81", "拣货策略"),
    WAREHOUSE_LOCATION("90", "仓位"),



    VIRTUAL_WAREHOUSE("86", "虚拟仓设置"),
    VIRTUAL_WAREHOUSE_ALLOCATION("87", "分货单"),
    PACKING_TASK("82", "装箱任务"),
    CARTON_SPC("83", "装箱箱规"),
    CARTON("84", "装箱信息"),
    CARTON_DETAIL("85", "装箱明细"),
    PICKING_LISTS("91", "拣货单"),
    CFG_RULE_WAVE("92", "波次规则"),
    WAREHOUSE_LOCATION_REPLENISH("93", "仓位补货"),

    CFG_RULE_COMMON("97", "规则设置"),
    REPLENISHMENT_SUGGESTION("98", "补货建议"),
    LABEL_INFO("99", "智能补货标签管理"),
    DELIVERY_SUGGEST("100", "发货建议"),
    PURCHASE_SUGGEST("101", "采购建议"),
    PURCHASE_SUGGEST_MERGE("102", "采购建议（合并）"),
    SUBCONTRACT_RETURN("94", "委外退料单"),
    INIT_FIRST_MILE_ALLOCATION("94", "期初费用分摊"),
    INVENTORY_SKU_COST("95", "SKU成本"),
    COST_ALLOCATION("96", "费用分摊"),
    PILOT_APPLICATION("97", "试产量产单"),

    DELIVERY_NOTICE_CHANGE("98", "发货通知变更单"),
    SO_B2C_RETURN("98", "b2c退货单"),
    REFUND_ORDER("99", "退款单"),
    DICT_RULE_CONDITION("110", "条件字典单"),
    RULE_CONDITION("111", "规则条件单"),
    CFG_RULE_VIRTUAL_TRANS("112", "虚拟库存交易规则设置"),
    REQUISITION_APPLICATION_CHANGE("113", "要货申请变更单"),
    WAVE_LIST("101", "波次列表"),
    PRODUCT_DETAIL("113", "产品明细"),
    REMOTE_POSTCODE("114", "偏远邮编"),
    FBA_TRANSIT_CALCULATE_REPORT("114", "FBA在途核对报表"),
    RULE_PROMPT_WORD("115", "汉化管理"),
    CFG_VAT_INVOICE("116", "VAT发票设置"),
    INVOICE_INFO("117", "发票上传记录"),

    CFG_SETTING("118", "系统配置"),
    SO_B2C_EXTEND("119", "销售订单扩展信息"),
    SO_PRICE("118", "销售价目表"),
    SO_PRICE_CHANGE("119", "销售调目表"),

    AFTER_SALE("120", "售后申请"),
    INVOICE_SETTING("121", "发票设置"),
    INVOICE_SETTING_DETAIL("122", "发票设置明细"),
    INVOICE_INVALID("123", "发票设置明细"),

    QC_NOTICE("124", "质检通知"),
    FIRST_MILE_CHANGE_RECORD("125", "头程调整记录"),
    CFG_RULE_INVOICE("126", "开票规则"),
    SKU_ORG_REF("127", "SKU与采购组织关系"),
    PROCESS_DELEGATE("125", "委托审批"),
    CFG_APPROVE_SYNC("126", "ERP审批同步配置"),
    CFG_PROCESS("127", "流程配置"),
    CFG_THIRD_PROCESS("128", "三方审批生成"),
    CFG_THIRD_NOTICE("129", "三方通知配置"),
    VIRTUAL_ADJUST("126", "虚拟库存调整"),
    CFG_SUPPLIER_SALES("130", "销量设置"),
    CONTRACT_INFO("128", "合同管理"),
    SUPPLIER_REF_WAREHOUSE("129", "仓库绑定"),
    CFG_RULE_INVOICE_PRODUCT_AMOUNT("130", "发票产品总价计算规则"),
    DICT_HS_CODE("131", "出口申报要素"),
    TEMPLATE_MANAGEMENT("132", "模板管理"),
    LOGISTICS_THIRD_CHANNEL_REF("130", "物流第三方渠道关系"),

    THIRD_WAREHOUSE_DELIVERY("131", "三方仓发货单"),


    ;



    private String code;
    private String name;

    ModuleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

}
