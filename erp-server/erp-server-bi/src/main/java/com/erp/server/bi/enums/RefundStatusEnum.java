package com.erp.server.bi.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 17:45
 */
public enum RefundStatusEnum {

    NEWREFUND(1, "新建退款"),
    UNDERREVIEW(2,"审核中"),
    FINANCIALREVIEW(3, "财务审核"),
    SUCCESS(4, "成功"),
    FAIL(5, "失败"),
    VOIDED(6, "作废");

    private Integer code;
    private String name;

    RefundStatusEnum(Integer code, String name) {
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
        for (RefundStatusEnum state : RefundStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        RefundStatusEnum[] refundStatusEnums = values();
        for (RefundStatusEnum refundStatusEnum : refundStatusEnums) {
            if (refundStatusEnum.getName().equals(name)) {
                return refundStatusEnum.getCode();
            }
        }
        return null;
    }
}
