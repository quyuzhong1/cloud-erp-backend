package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 17:45
 */
public enum RefundEnum {

    NEWREFUND(1, "新建退款"),
    UNDERREVIEW(2,"审核中"),
    FINANCIALREVIEW(3, "财务审核"),
    SUCCESS(4, "成功"),
    FAIL(5, "失败"),
    VOIDED(6, "作废");

    private Integer code;
    private String name;

    RefundEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static String getName(Integer code) {
        for (RefundEnum state : RefundEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
