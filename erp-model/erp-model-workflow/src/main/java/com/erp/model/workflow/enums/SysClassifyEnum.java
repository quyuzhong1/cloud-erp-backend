package com.erp.model.workflow.enums;

public enum SysClassifyEnum {
    PLM("plm", "PLM系统"),
    SCM("scm", "SCM系统"),
    WMS("wms", "WMS系统");

    private String code;
    private String name;

    SysClassifyEnum(String code, String name) {
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

    public static SysClassifyEnum getEnumByCode(String code) {
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }
}
