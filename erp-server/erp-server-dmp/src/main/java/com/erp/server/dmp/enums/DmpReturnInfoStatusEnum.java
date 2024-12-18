package com.erp.server.dmp.enums;

public enum  DmpReturnInfoStatusEnum {

    PENDING(1, "待处理"),
    REFUNDED(2,"已退款"),
    RESEND(3, "已重发"),
    VOIDOMPLETEDED(4, "已完成"),
    VOIDED(5, "已作废");

    private Integer code;
    private String name;

    DmpReturnInfoStatusEnum(Integer code, String name) {
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
        for (DmpReturnInfoStatusEnum state : DmpReturnInfoStatusEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }
}
