package com.erp.model.scm.enums;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum OrderConfirmOperatorTypeEnum {

    //系统
    AUTO("auto", "系统"),
    //手动
    MANUAL("manual", "手动");


    private String code;
    private String name;

    OrderConfirmOperatorTypeEnum(String code, String name) {
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
        OrderConfirmOperatorTypeEnum[] stateEnums = values();
        for (OrderConfirmOperatorTypeEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
