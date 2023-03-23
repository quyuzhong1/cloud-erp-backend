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
    SUPPLIER("3", "供应商");


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
