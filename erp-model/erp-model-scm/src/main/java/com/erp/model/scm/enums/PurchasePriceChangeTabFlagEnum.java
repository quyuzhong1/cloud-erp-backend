package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum PurchasePriceChangeTabFlagEnum {

    //待我审核
    APPROVE_ING("approveIng", "待我审核"),
    //已审核
    APPROVE("approve", "已审核"),
    //不通过
    REJECT("reject", "不通过"),
    ;


    private String code;
    private String name;

    PurchasePriceChangeTabFlagEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        PurchasePriceChangeTabFlagEnum[] stateEnums = values();
        for (PurchasePriceChangeTabFlagEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
