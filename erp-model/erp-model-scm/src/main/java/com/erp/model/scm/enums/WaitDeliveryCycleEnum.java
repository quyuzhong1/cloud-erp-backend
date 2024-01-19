package com.erp.model.scm.enums;

/**
 * 待交货周期枚举
 * @author zdy
 * @version 1.0

 * @date 2023/3/27 10:52
 */
public enum WaitDeliveryCycleEnum {

    //已超期
    EXPIRED("expired", "已超期"),
    //即将超期
    ALMOST_OVERDUE("almostOverdue", "即将超期"),
    //1个月内
    IN_ONE_MONTH("inOneMonth", "1个月内"),
    //2个月内
    IN_TWO_MONTH("inTwoMonth", "2个月内"),
    // 2个月以后
    TWO_MONTH_LATER("twoMonthLater", "2个月以后")
    ;


    private String code;
    private String name;

    WaitDeliveryCycleEnum(String code, String name) {
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
        WaitDeliveryCycleEnum[] stateEnums = values();
        for (WaitDeliveryCycleEnum stateEnum : stateEnums) {
            if (stateEnum.getCode().equals(code) ) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
