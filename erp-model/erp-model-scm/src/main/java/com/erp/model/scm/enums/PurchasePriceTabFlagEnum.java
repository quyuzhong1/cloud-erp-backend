package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum PurchasePriceTabFlagEnum {

    //待我审核
    APPROVE_ING("approveIng", "待我审核"),
    //已确认
    REJECT("reject", "不通过"),
    //已审核启用
    APPROVE_ENABLE("approveEnable", "已审核启用"),
    //已审核停用
    APPROVE_DISABLED("approveDisabled", "已审核停用"),
    ;


    private String code;
    private String name;

    PurchasePriceTabFlagEnum(String code, String name) {
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
        PurchasePriceTabFlagEnum[] stateEnums = values();
        for (PurchasePriceTabFlagEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
