package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
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
