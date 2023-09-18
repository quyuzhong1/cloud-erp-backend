package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 比较符枚举
 * @date 2022/12/30 11:48
 */
public enum BiCompareEnum {


    GREATER_THAN_EQUAL(">=", "大于等于"),
    LESS_THAN_EQUAL("<=", "小于等于"),
    GREATER_THAN(">", "大于"),
    LESS_THAN("<", "小于"),
    TOW_MONTH_GREATER_THEN_EQUAL("towMonthGreaterThenEqual", "连续两个月大于等于"),
    TOW_MONTH_LESS_THEN_EQUAL("towMonthLessThenEqual", "连续两个月小于等于")
    ;

    private String code;

    private String name;

    BiCompareEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        for (BiCompareEnum biCompareEnum : BiCompareEnum.values()) {
            if (biCompareEnum.getCode().equals(code)) {
                return biCompareEnum.getName();
            }
        }
        return "";
    }

    public static String getCodeByName(String name) {
        BiCompareEnum[] enums = values();
        for (BiCompareEnum biCompareEnum : enums) {
            if (biCompareEnum.getName().equals(name)) {
                return biCompareEnum.getCode();
            }
        }
        return null;
    }

}
