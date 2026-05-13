package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 采购订单枚举类型
 * @date 2023/8/4 10:29
 */
public enum PurchaseOrderTypeEnum {

    ENUM_PURCHASE("CGDD01_SYS", "标准采购订单"),
    ENUM_SUBCONTRACT("CGDD02_SYS", "委外采购订单"),
    ENUM_RETURN("CGDD06-SYS", "补货采购订单"),
    ENUM_REPAIR("CGDD99_SYS", "返修采购订单"),
            ;

    private String code;
    private String name;

    PurchaseOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        PurchaseOrderTypeEnum[] stateEnums = values();
        for (PurchaseOrderTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
