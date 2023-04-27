package com.erp.model.workflow.enums;

public enum ModelTypeEnum {
    OFTEN("1", "常用模块"),
    WAITDO("2", "代办模块");

    private String code;
    private String name;

    ModelTypeEnum(String code, String name) {
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
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

}
