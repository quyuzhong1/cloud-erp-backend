package com.erp.server.dmp.enums;

public enum DmpRefundInfoStatusEnum {
    SUCCESS("1", "已成功"),
    FAIL("2", "已失败"),
    INVALID("3", "已作废");

    private String code;
    private String name;

    DmpRefundInfoStatusEnum(String code, String name) {
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
        for (DmpRefundInfoStatusEnum state : DmpRefundInfoStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
