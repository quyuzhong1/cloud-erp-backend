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
    OVERSEAS_DELIVERY_PLAN("54", "海外发货计划"),
    REQUISITION_APPLICATION("55", "要货申请"),
    OVERSEAS_PROVIDER("56", "仓库设置"),
    OVERSEAS_WAREHOUSE_INBOUND("57", "海外仓入库单"),
    SO_B2C_DELIVERY("58", "b2c发货单"),
    SO_B2C_DELIVERY_INTERCEPT("59", "b2c发货拦截单"),
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
